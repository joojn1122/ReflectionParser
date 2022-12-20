package com.joojn.reflectionparser.util;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AdvancedReflector {

    private final  Class<?>  clazz       /*   */ ;
    private /* */  String    name        = null  ;
    private /* */  boolean   allClasses  = false ;

    private AdvancedReflector(Class<?> clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Returns Reflector from class
     * @return this
     * @author joojn
     */
    public static AdvancedReflector For(Class<?> clazz)
    {
        return new AdvancedReflector(clazz);
    }

    /**
     * Returns Reflector from class loaded using system cl
     * @return this
     * @author joojn
     */
    public static AdvancedReflector For(String className)
    {
        return For(className, ClassLoader.getSystemClassLoader());
    }

    /**
     * Returns Reflector from class loaded using cl
     * @return this
     * @author joojn
     */
    public static AdvancedReflector For(String className, ClassLoader cl)
    {
        try
        {
            return For(cl.loadClass(className));
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets name of field / method to search for
     * @param name name of field / method
     * @return this
     */
    public AdvancedReflector name(String name)
    {
        this.name = name;
        return this;
    }

    /**
     @param find if set to <code>true</code>, it searches through every super class with `getDeclared`
     @return this
     **/
    public AdvancedReflector searchAll(boolean find)
    {
        this.allClasses = find;
        return this;
    }

    /**
     * Executes lambda if exists
     * @return ExistExpression allows usage of orElse / orElseThrow methods
     */
    public ExistExpression<AdvancedReflector> exists(
            ExistExpression.ExpressionCheck<AdvancedReflector> ifExists
    )
    {
        return new ExistExpression<>(
                simpleExists(),
                ifExists,
                this
        );
    }

    /**
     * Returns if field exists
     * @return if field is not null
     */
    public boolean simpleExists()
    {
        return findField(clazz, name) != null;
    }

    /**
     * Sets current instance of field
     * @param instance instance of field, null for static
     * @return FieldReflector
     */
    public FieldReflector with(Object instance)
    {
        return new FieldReflector(instance);
    }

    /**
     * Gets field reflector of static field
     * @return FieldReflector
     */
    public FieldReflector withStatic()
    {
        return with(null);
    }

    public class FieldReflector {

        private final Object instance;
        private final Field field;

        private FieldReflector(Object instance)
        {
            this.field = findField(
                    AdvancedReflector.this.clazz,
                    AdvancedReflector.this.name
            );
            field.setAccessible(true);

            this.instance = instance;
        }

        /**
         * Gets value from field
         * @return value from field
         * @param <T> field return type
         */
        public <T> T get()
        {
            try
            {
                return (T) field.get(instance);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * Gets value from field
         * @return value from field
         * @param <T> field return type
         */
        public <T> T get(Class<T> tClass)
        {
            return get();
        }

        /**
         * Sets value of field
         * @param value new value of field
         */
        public void set(Object value)
        {
            try
            {
                field.set(instance, value);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Tries to find method from given name and classes
     * @param classes method params
     * @return MethodReflector from classes
     */
    public MethodReflector usingArgs(Class<?>... classes)
    {
        return new MethodReflector(classes);
    }

    /**
     * Tries to find method from given name? and return type
     * @param clazz return type of method
     * @param index if name is null, it looks for multiple methods, index indicates which method should be selected
     * @return MethodReflector from classes
     */
    public MethodReflector returningWithIndex(Class<?> clazz, int index)
    {
        int[] currentIndex = { 0 };
        Method[] foundedMethod = new Method[1];

        classWalker(this.clazz, (currentClass) -> {
            for(Method method : currentClass.getDeclaredMethods())
            {
                if((name == null || method.getName().equals(name)) && method.getReturnType().equals(clazz) && currentIndex[0]++ == index)
                {
                    foundedMethod[0] = method;
                    return true;
                }
            }

            return false;
        });

        return new MethodReflector(foundedMethod[0]);
    }

    /**
     * Tries to find method from given name? and return type
     * @param clazz return type of method
     * @return MethodReflector from classes
     */
    public MethodReflector returning(Class<?> clazz)
    {
        return returningWithIndex(clazz, 0);
    }

    public class MethodReflector {

        private final Method method;
        private Object instance;

        private MethodReflector(Class<?>[] classes)
        {
            this(
                    findMethod(AdvancedReflector.this.clazz, AdvancedReflector.this.name, classes)
            );
        }

        private MethodReflector(Method method) {
            this.method = method;
        }

        /**
         * Sets instance of method
         * @param instance instance of method
         * @return this
         */
        public MethodReflector with(Object instance)
        {
            this.instance = instance;
            method.setAccessible(true);

            return this;
        }

        /**
         * Sets instance of method to static
         * @return this
         */
        public MethodReflector withStatic()
        {
            return with(null);
        }

        /**
         * Invokes searched method using given arguments
         * @param args arguments of method
         * @return value returned by method
         * @param <T> data type of #{@return}
         */
        public <T> T invoke(Object... args)
        {
            try
            {
                return (T) method.invoke(instance, args);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * Executes lambda if method exists
         * @return ExistExpression allows usage of orElse / orElseThrow methods
         */
        public <T> ExistExpression<MethodReflector> exists(
                ExistExpression.ExpressionCheck<MethodReflector> ifExists
        )
        {
            return new ExistExpression<>(
                    simpleExists(),
                    ifExists,
                    this
            );
        }

        /**
         * Returns if method exists
         * @return if method is not null
         */
        public boolean simpleExists()
        {
            return this.method != null;
        }
    }

    private interface ClassWalker {
        boolean walk(Class<?> clazz);
    }

    private void classWalker(Class<?> topClass, ClassWalker walker)
    {
        if(!allClasses)
        {
            walker.walk(topClass);
            return;
        }

        Class<?> currentClass = topClass;
        do
        {
            if(walker.walk(currentClass)) break;
        }
        while((currentClass = currentClass.getSuperclass()) != Object.class);
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... classes)
    {
        Method[] method = new Method[1];

        classWalker(clazz, (currentClass) -> {
            try
            {
                method[0] = currentClass.getDeclaredMethod(name, classes);
                return true;
            }
            catch (NoSuchMethodException ignored) {}

            return false;
        });

        return method[0];
    }

    private Field findField(Class<?> clazz, String name)
    {
        Field[] field = new Field[1];

        classWalker(clazz, (cs) -> {
            try
            {
                field[0] = cs.getDeclaredField(name);
                return true;
            }
            catch (NoSuchFieldException ignored) {}

            return false;
        });

        return field[0];
    }

    // Exist Expression
    public static class ExistExpression<T>
    {
        public interface ExpressionCheck<T>
        {
            Object execute(T value);
        }

        public interface VoidExpressionCheck<T>
        {
            void execute(T value);
        }

        private final boolean            exists;
        private final T                  value;
        private /* */ Object             returnedValue = null;

        public ExistExpression(boolean exists, ExpressionCheck<T> check, T value) {
            this.exists = exists;
            this.value = value;

            if(exists) {
                this.returnedValue = check.execute(value);
            }
        }

        /**
         * If value is value, executes current lambda expression
         * @param orElse lambda expression
         */
        public ExistExpression<T> orElse(VoidExpressionCheck<T> orElse)
        {
            if(!exists) orElse.execute(value);

            return this;
        }

        /**
         * If value is value, throws #NotExistException with custom message
         * @param exception exception to be thrown
         */
        public ExistExpression<T> orElseThrow(RuntimeException exception)
        {
            if(!exists) throw exception;

            return this;
        }

        /**
         * Returns the returned value from exists method
         * @return returnedValue
         * @param <S> data type of returnedValue
         */
        public <S> S build()
        {
            return (S) this.returnedValue;
        }
    }

}