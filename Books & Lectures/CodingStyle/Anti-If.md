
# Anti-If: the missing patterns

[Source](http://code.joejag.com/2016/anti-if-the-missing-patterns.html?utm_source=wanqu.co&utm_campaign=Wanqu+Daily&utm_medium=website)

Problem with If

- easy to read at first, become difficult as CodeBlock grow and coupling of sharedState become difficult
- when duplicated, domain concept is missing
- you have to simulate execution in your head

### I.Boolean Pattern

Context: You have a method that takes a boolean which alters its behavior

```java
public void example() {
    FileUtils.createFile("name.txt", "file contents", false);
    FileUtils.createFile("name_temp.txt", "file contents", true);
}

public class FileUtils {
    public static void createFile(String name, String contents, boolean temporary) {
        if(temporary) {
            // save temp file
        } else {
            // save permanent file
        }
    }
}
```

Tolerance: Usually when you see this context you can work out at compile time which path the code will take. If that is the case then always use this pattern.

Solution: Split the method into two new methods
```java
public void example() {
    FileUtils.createFile("name.txt", "file contents");
    FileUtils.createTemporaryFile("name_temp.txt", "file contents");
}

public class FileUtils {
    public static void createFile(String name, String contents) {
        // save permanent file
    }

    public static void createTemporaryFile(String name, String contents) {
        // save temp file
    }
}
```

### II. Switch to Polymorphism

Context: You are switching based on type.

```java
public class Bird {

    private enum Species {
        EUROPEAN, AFRICAN, NORWEGIAN_BLUE;
    }
    private boolean isNailed;
    private Species type;

    public double getSpeed() {
        switch (type) {
            case EUROPEAN:
                return getBaseSpeed();
            case AFRICAN:
                return getBaseSpeed() - getLoadFactor();
            case NORWEGIAN_BLUE:
                return isNailed ? 0 : getBaseSpeed();
            default:
                return 0;
        }
    }

    private double getLoadFactor() {
        return 3;
    }

    private double getBaseSpeed() {
        return 10;
    }
}
```

Tolerance: A single switch on type is fine. It’s when their are multiple switches then bugs can be introduced as a person adding a new type can forget to update all the switches that exist on this hidden type.

Solution: Use Polymorphism. Anyone introducing a new type cannot forget to add the associated behaviour

```java
public abstract class Bird {
    public abstract double getSpeed();
    protected double getLoadFactor() {return 3;}
    protected double getBaseSpeed() {return 10;}
}

public class EuropeanBird extends Bird {
    public double getSpeed() {
        return getBaseSpeed();
    }
}

public class AfricanBird extends Bird {
    public double getSpeed() {
        return getBaseSpeed() - getLoadFactor();
    }
}

public class NorwegianBird extends Bird {
    private boolean isNailed;
    public double getSpeed() {
        return isNailed ? 0 : getBaseSpeed();
    }
}
```


### III.NullObject over null passing

Context: A outsider asked to understand the primary purpose of your code base answers with “to check if things equal null”.

```java
public void example() {
    sumOf(null);
}

private int sumOf(List<Integer> numbers) {
    if(numbers == null) {
        return 0;
    }
    return numbers.stream().mapToInt(i -> i).sum();
}
```

Solution: Use a NullObject or Optional type instead of ever passing a null. An empty collection is a great alternative.

```java
public void example() {
    sumOf(new ArrayList<>());
}

private int sumOf(List<Integer> numbers) {
    return numbers.stream().mapToInt(i -> i).sum();
}
```


### IV. Inline statements into expressions

Context: You have an if statement tree that calculates a boolean expression.
```
public boolean horrible(boolean foo, boolean bar, boolean baz) {
    if (foo) {
        if (bar) {
            return true;
        }
    }

    if (baz) {
        return true;
    } else {
        return false;
    }
}
```

Solution: Simplify the if statements into a single expression.
```
public boolean horrible(boolean foo, boolean bar, boolean baz) {
    return foo && bar || baz;
}
```

### V. Give a coping strategy

Context: You are calling some other code, but you aren’t sure if the happy path will succeed.

```java
public class Repository {
    public String getRecord(int id) {
        return null; // cannot find the record
    }
}

public class Finder {
    public String displayRecord(Repository repository) {
        String record = repository.getRecord(123);
        if(record == null) {
            return "Not found";
        } else {
            return record;
        }
    }
}
```

Tolerance: It’s better to push this if statement into one place, so it isn’t duplicated and we can remove the coupling on the empty object magic value.

```java
private class Repository {
    public String getRecord(int id, String defaultValue) {
        String result = Db.getRecord(id);

        if (result != null) {
            return result;
        }

        return defaultValue;
    }
}

public class Finder {
    public String displayRecord(Repository repository) {
        return repository.getRecord(123, "Not found");
    }
}
```
