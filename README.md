# Simple Reflection Parser

Parsing file in java
```
File f = new File("filename");
ReflectionParser.parseFile(f);
```

Or you can parse lines
```
ReflectionParser.parseLine("class Test = org.test.Test");
```

You can see example of syntax in file `example.txt`

Comments are allowed only at the start of the line using `//`

---
### Classes

We can create a shortcut for classes
```
class System = java.lang.System

string text = "Hello World"
void _ = System->out->println(%text%)
```

_or without shortcut_

```
string text = "Hello World"
void _ = java.lang.System->out->println(%text%)
```
---
### **Variables**

For variables, we use `%variable%`
```
class System = java.lang.System

string name = "John"
void _ System->out->println(%name%)
```

Unfortunately we can't use `System->out->println("John")`
because it's not implemented yet.

---
### **Objects**

These keywords to create object:
  - `object` - create any object
  - `void` - ignore value of return (example void method)

To create java objects:
  - `string` - create a new string (has to be capital s)
  - `integer / int` - create a new integer
  - `char / character` - create a new character
  - `boolean` - create a new boolean

Example:
```
class String = java.lang.String

// init new variables
string test = "test"
char ch = 'a'
int i = 0

// it's still string but it's not being inicialized so it's object
object test2 = String->valueOf(%i%)
```
