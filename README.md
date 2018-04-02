# NormalizeRecursion (Circular dependency)
Avoid Java objects to have repeated references to the objects that cause a recursion problem on a possible serialization

## Problem
When we try to serialize a Java object to JSON and it has recursive references, check the example:

```java
class Person {
  //...
  public Car getCars();
  //...
}

class Car {
  //...
  public User getOwner();
  //...
}
```
When we try to serialize it using Gson, Jackson or any other framework or library we will probably faced with a ```StackOverflow``` exception, it means that the object we are trying to serialize propably has a "circular dependency", an object A that points to object B and object B also points to object A, in serialization causes a infinity recursion.

A solution is to avoid some fields with @annotation, other is manually set null in this "circular fields".

The propose of the next algorithmn is to detect with deep iteration and reflection "circular fields" and automatically set it to null.
