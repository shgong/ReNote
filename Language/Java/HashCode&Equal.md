# Java Example


## 1. hashCode()

Overriding.

```java

public class Tiger {
    private String color;
    private String stripePattern;
    private int height;

    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            Tiger tiger = (Tiger) object;
            if (this.color == tiger.getColor()
                    && this.stripePattern == tiger.getStripePattern()) {
                result = true;
            }
        }
        return result;
    }

    // just omitted null checks
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.color.hashCode();
        hash = 7 * hash + this.stripePattern.hashCode();
        return hash;
    }
}
```
