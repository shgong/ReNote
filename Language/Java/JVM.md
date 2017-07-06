
# Hacking JVM

### Question
How you real life work would be: a question -> find a answer.
Can you take the solution and implement it?

### Printing Arrays Problem

Unlike other JDK classes, arrays don't have a particularly sane toString() as it is inherited from Object.

It prints the internal representation of the type, and the hashCode() of the object.  As all arrays are an Object, they have a hashCode() and a type and a synchronized lock, and every thing else an Object has, but no methods specific to an array. This is why the toString() isn't useful for arrays.

If run program test.java
```java
public class test {
    boolean[] booleans = {true, false};
    byte[] bytes = {1, 2, 3};
    char[] chars = "Hello World".toCharArray();
    short[] shorts = {111, 222, 333};
    float[] floats = {1.0f, 2.2f, 3.33f, 44.44f, 55.555f, 666.666f};
    int[] ints = {1, 22, 333, 4_444, 55_555, 666_666};
    double[] doubles = {Math.PI, Math.E};
    long[] longs = {System.currentTimeMillis(), System.nanoTime()};
    String[] words = "The quick brown fox jumps over the lazy dog".split(" ");

    public void testToString() throws IllegalAccessException {
        Map<String, Object> arrays = new LinkedHashMap<>();
        for(Field f : getClass().getDeclaredFields())
            arrays.put(f.getName(), f.get(this));
        arrays.entrySet().forEach(System.out::println);
    }
}
```

Will print
```
booleans=[Z@277c0f21
bytes=[B@6073f712
chars=[C@43556938
shorts=[S@3d04a311
floats=[F@7a46a697
ints=[I@5f205aa
doubles=[D@6d86b085
longs=[J@75828a0f
words=[Ljava.lang.String;@3abfe836
```

What we can do is change the `Object.toString()`. We have to change this class as it is the only parent of arrays we have access to. We cannot change the code for an array as it is internal to the JVM.  There is no byte[] java class file for example for all the byte[] specific methods.

-----

### Step-by-Step Solution

#####1. Take a copy of the source for java.lang.Object and replace the toString() method with

```java
public String toString() {
        if (this instanceof boolean[])
            return Arrays.toString((boolean[]) this);
        if (this instanceof byte[])
            return Arrays.toString((byte[]) this);
        if (this instanceof short[])
            return Arrays.toString((short[]) this);
        if (this instanceof char[])
            return Arrays.toString((char[]) this);
        if (this instanceof int[])
            return Arrays.toString((int[]) this);
        if (this instanceof long[])
            return Arrays.toString((long[]) this);
        if (this instanceof float[])
            return Arrays.toString((float[]) this);
        if (this instanceof double[])
            return Arrays.toString((double[]) this);
        if (this instanceof Object[])
            return Arrays.deepToString((Object[]) this);
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
```

import `java.util.Arrays` if not found.


##### 2. Compile the class and add it to a jar

```
javac java/lang/Object.class
jar cvf  modified-rt.jar java/lang/Object.class
```

##### 3. in Java <= 8 we can add this class to the start of the bootclasspath by adding to the command line with `-Xbootclasspath/p`

run in terminal
```
java -Xbootclasspath/p:modified-rt.jar test
```

and now we see
```
booleans=[true, false]
bytes=[1, 2, 3]
chars=[H, e, l, l, o,  , W, o, r, l, d]
shorts=[111, 222, 333]
floats=[1.0, 2.2, 3.33, 44.44, 55.555, 666.666]
ints=[1, 22, 333, 4444, 55555, 666666]
doubles=[3.141592653589793, 2.718281828459045]
longs=[1457629893500, 1707696453284240]
words=[The, quick, brown, fox, jumps, over, the, lazy, dog]
```


----

Reference:
[Having fun with Java Lang Objects](http://blog.vinceliu.com/2007/07/having-fun-with-javalangobject.html)
[Printing Arrays By Hacking JVM](http://vanillajava.blogspot.com/2016/03/printing-arrays-by-hacking-jvm.html)

