# Learn from Facebook 60 TB Spark use case

After read the Facebook’s engineer blog about Spark 60TB use case (https://code.facebook.com/posts/1671373793181703/apache-spark-scale-a-60-tb-production-use-case/), feel interesting to look at a little more detail about the bugs they found, how they fixed it, also for the purposes:
- get some experience for the bugs coming from the extreme run time condition
- more familiar with Spark source code

Let’s start it:

## Spark-14363
https://issues.apache.org/jira/browse/SPARK-14363

Fix memory leak in the Sorter. When the UnsafeExternalSorter spills the data to disk, it does not free up the underlying pointer array. As a result, we see a lot of executor OOM and also memory under utilization.

The actual codes changed are in the class ShuffleExternalSorter, which call by the UnsafeShuffleWriter. When an Executor process a ShuffleMapTask, it use the ShuffleWriter to write (sorting) the data with its partition information to output files. The underlying pointer array mentioned in their description is a array of LongArray type inside of ShuffleInMemorySorter. This array is used to stock all the records’s pointer. (I have explain the detail about the LongArray and the 64bit long pointer in this blog). The benefit is, the sorting by partitions could process on this array instead of the record directly. This is an improvement being part of Tungsten project.
In each spill() calling, ShuffleExternalSorter call ShuffleInMemorySorter to do the sorting and write the sorting data to disk. The bug is ShuffleExternalSorter only call ShuffleInMemorySorter’s reset() method when write the LAST spilled file. In most of case it’s Ok, because the LongArray is for saving the 64 bit long pointers. But FB team found that if one task (ShuffleInMemorySorter) continue on taking more and more memory, for example some other task finished in the same Executor, this LongArray will take significant memory, and specially, as far as this spilled task not totally finished, this LongArray memory will NEVER be released. This cause others new tasks running on this Executor having OOM. It will be good that FB team post the full stack of the OOM exception.

The change is very simple. Call ShuffleInMemorySorter’s reset() method in every time spill() has been called in ShuffleExternalSorter.

Learned: When you allocate the JVM heap memory or off-heap memory, always release it ASAP. Specially when your allocation can extend dynamically. For example in the expandPointerArray() of ShuffleInMemorySorter, we always double the current size of ArrayLong. It’s better to set up a limitation. I found the late commits on ShuffleExternalSorter, they added a numElementsForSpillThreshold for this pointers ArrayLong. by default is 8G of ArrayLong, once pass this threshold, spill the records to disk immediately which release the whole memory allocated by this pointers array now.

## Spark-14277
https://issues.apache.org/jira/browse/SPARK-14277

A JNI method — (Snappy.ArrayCopy) — was being called for each row being read/written. We raised this issue, and the Snappy behavior was changed to use the non-JNI based System.ArrayCopy instead.
UnsafeSorterSpillReader read spilled file record by record. And this end by calling a lot of Snappy.arrayCopy() (a JNI method), which cause the CPU overhead problem. Good to know that the java native method System.arrayCopy is implemented by VM intrinsic native code (Unsafe methods are intrinsic, meaning each method call is compiled by JIT into a single machine instruction.), so very very fast and not a JNI call. The fix is in snappy-java library side.

## Spark-5581
https://issues.apache.org/jira/browse/SPARK-5581

On the map side, when writing shuffle data to disk, the map task was opening and closing the same file for each partition. We made a fix to avoid unnecessary open/close and observed a CPU improvement of up to 50 percent for jobs writing a very high number of shuffle partitions.

Hehe, looks like Spark team know this issue for a while, just nobody want to implement this improvement :p

ShuffleExternalSorter(again …) use DiskBlockObjectWriter to write some bytes to the disk. The important feature of this writer is could append data atomicitly, which is used to write data from different partitions into one sorted output file.

The Sorter need write the records to disk one by one when spill happen(too many memory pressure from running Executor) or before it close. During the spilling, every time if the record to write switch partition, Sorter has to call commitAndClose() of Writer. In fact, the records to write are already sorted by partition in this moment, but in the FB use case, some Spark stage contain more than 100k partition, in the worst case, this commitAndClose() will cost a lot of CPU time.

In the new implementation, Sorter only call commit() if it need to switch the partitions. The changes inside of DiskBlockObjectWriter include add streamOpen flag, commitPosition pointer, ManualCloseBufferedOutputStream class which hack a little the close() and flush() of generic OutputStream …

I have not read and understand each line changed in the Writer. But apparently a big wins.

## Spark-14649
https://issues.apache.org/jira/browse/SPARK-14649
The Spark driver was resubmitting already running tasks when a fetch failure occurred, which led to poor performance. We fixed the issue by avoiding rerunning the running tasks.

This is a Controversial fix, until now, the PR is not included in the Spark master. In this ticket, we are talking bout only the shuffle fetching tasks’s re-run. The current implementation is DAGScheduler force to re-run all the not finished fetch tasks if one fetch failure occurred. Sital Kedia from FB want to the running fetch tasks keep running even one fetch failure occurred. Because this running task could still be finished and don’t need to submit to DAGScheduler for re-run. But Kay Ousterhout from Berkeley pointed out: if current running task failed late, when they re-run, they can’t fetch the old map task’s output anymore, because the corresponded map task has re-run trigger by the failed fetch task before … (but I think in this case this task could re-run as the other fetch failed tasks, maybe this create a not finished loops?)

Handling the failure need be very careful in the software development, in the context of shuffle, it’s just much complicated. I can understand Sital Kedia’s pain and agree with his idea, but I’m not very familiar in which condition a fetch failed task could re-trigger the map task and re-run. May looks at the source late to learn the detail about this subject. But at least now, better understand what happen when a fetch task failed.

## Spark-15074
https://issues.apache.org/jira/browse/SPARK-15074
We found out that the shuffle service is opening/closing the shuffle index file for each shuffle fetch. We made a change to cache the index information so that we can avoid file open/close and reuse the index information for subsequent fetches.

The index files are generated by the mapper task to help the fetch task load the correct file segment part from one single mapper output file. Each reducer has to read a 8 bytes offset value saved in the index file. So even with 10k reducers, the index file size still very small. It’s suitable to be cached in the memory. The index file name is in format of shuffle_ShuffleId_MapId_0.index, and the data file name is in format of shuffle_ShuffleId_MapId_0.data. All the reading index file logic has been implemented in getSortBasedShuffleBlockData() of ExternalShuffleBlockResolver.

In front of ExternalShuffleBlockResolver is ExternalShuffleBlockHandler which handle all the RPC call to fetch the shuffle block or register a Executor.

The fix is using com.google.common.cache.LoadingCache to cache a ShuffleIndexInformation for each mapper out file. So each reducer can fetch a shuffle block data without open and close the index file.

## Spark-15958
https://issues.apache.org/jira/browse/SPARK-15958
The default initial buffer size for the Sorter is too small (4 KB), and we found that it is very small for large workloads — and as a result we waste a significant amount of time expending the buffer and copying the contents.
They talk about the DEFAULT_INITIAL_SORT_BUFFER_SIZE inside of UnsafeExternalRowSorter. Follow on the sources, this buffer size finally pass to DiskBlockObjectWriter’s constructor.

You also have DISK_WRITE_BUFFER_SIZE in ShuffleExternalSorter with the default value 1024k which use to buffer the bytes write to the memory, also use in the write() method of DiskBlockObjectWriter. Generally feel the Spark’s buffer config are too many and from the name is very difficult to know what is will be used for.

## Spark-15569
https://issues.apache.org/jira/browse/SPARK-15569
Using the Spark Linux Perf integration, we found that around 20 percent of the CPU time was being spent probing and updating __the shuffle bytes written metrics__.
This one is easy to understand and fix. Key is to spot this bad line of code. Just change DiskObjectWriter.recordWritten() method from :

```
if (numRecordsWritten % 32 == 0) {
  updateBytesWritten()
}
to
if (numRecordsWritten % 16384 == 0) {
  updateBytesWritten()
}
```

updateBytesWritten() is used to just update the write metrics.

## Summary
I have went through almost all the performance optimization fix and improvements in the blog. Most of them related to the shuffle stage. The performance of shuffle is the key for the big data processing, Spark already did a good job. But this time, under the 60TB real data context, FB team found some difficult bugs and problems, with all their PR, just make Spark better. But most of their fixing are only included in 2.1.0 version.
