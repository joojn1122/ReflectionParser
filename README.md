# Simple Reflection Parser

Parsing file in java
```java
File file = new File("Example.txt");

ReflectionParser parser = new ReflectionParser();
Files.readAllLines(file.toPath()).forEach(parser::parseLine);
```

You can change comments to anything you want, default is "#"
```java
new ReflectionParser("//");
```

---
### Classes

Importing classes is simple
```
import java.lang.reflect.Field

# print field class
System.out.println(Field)
```
Unlike java, these classes are already class objects

You can also alias imports to
make them easier to use or when you have 
multiple imports with the same name, because
unlike java you can't type whole package name

```
import java.lang.reflect.Field as F

System.out.println(F)

# this throws an error
System.out.println(java.lang.reflect.Field)
```
---
### **Variables**

Unlike java, variables are not declared and they don't have data types
```
name = "John"
System.out.println(name.getClass())
# java.lang.String
```

---
### **Constructors**
Constructors are same as methods, but
instead of using "new" you simply call
it as method but with these brackets "<>"
```
# create new object
object = Object<>
System.out.println(object.toString())
```

Also in methods and constructors you can 
specify parameter types like this
```
System.out.println(null: Object)
```
_Without the "Object" type, it would crash
(NullPointerException), because it doesn't 
know which method to call_

---
### **Arithmetics**

Arithmetics are not implemented, but there is a helper class
```
import com.joojn.reflectionparser.util.MathUtil

a = 10
b = 20

System.out.println(MathUtil.add(a, b))
# 30
```
