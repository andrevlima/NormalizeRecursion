# NormalizeRecursion (Circular dependency)
Avoid Java field of objects that have repeated references to objects that somewhere points again to this (aka circular dependency)

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

Person person = new Person();
Car car = new Car();
car.setOwner(person);
person.getCars().add(car);


```
When we try to serialize it using Gson, Jackson or any other framework or library we will probably get faced with a ```StackOverflow``` exception, it means that the object we are trying to serialize propably has a "circular dependency", an object A that points to object B and object B also points to object A, in serialization causes a infinity recursion, but for Java it's all ok because it works "by reference" but in a serialization process, fields are visited deep and how deep is a loop? it's so as big as ```StackOverflow``` appears :)

A solution is to avoid some fields with @annotation, other is manually set null in this "circular fields".

The propose of the next algorithmn is to detect with deep iteration and reflection "circular fields" and automatically set it to null.
