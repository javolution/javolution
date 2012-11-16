/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ENUM_HPP
#define _JAVOLUTION_LANG_ENUM_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/Class.hpp"
#include "javolution/util/FastTable.hpp"
#include "javolution/lang/IllegalArgumentException.hpp"

namespace javolution {
    namespace lang {
        class Enum_ANY_API;
        typedef Type::Handle<Enum_ANY_API> Enum_ANY;
        template<class E> class Enum_API;
        template<class E> class Enum;
    }
}

/**
 * This is the common base class of all enumeration types.
 * Enumerate are often declared within a class definition, for example.
 * [code]
 *
 * // Color.hpp header file.
 * class Color_API;
 * typedef javolution::lang::Enum<Color_API> Color; // Public shortcut.
 * class Color_API : public javolution::lang::Enum_API<Color_API> {
 * public:
 *     static const Color UNKNOWN;
 *     static const Color BLUE;
 *     static const Color GREEN;
 *     static Type::Array<Color> values();
 * };
 *
 * // Color.cpp implementation file.
 * using namespace javolution::lang;
 * using namespace myPkg;
 * const Class<Color_API> COLOR_CLASS = Class_API<Color_API>::forName(L"myPkg::Color_API");
 * const Color Color_API::UNKNOWN = Enum_API<Color_API>::newInstance(COLOR_CLASS, L"unknown", -1); // Explicit ordinal value.
 * const Color Color_API::BLUE = Enum_API<Color_API>::newInstance(COLOR_CLASS, L"blue");
 * const Color Color_API::GREEN = Enum_API<Color_API>::newInstance(COLOR_CLASS, L"green");
 * Type::Array<Color> COLOR_VALUES = Enum_API<Color_API>::values(COLOR_CLASS); // Must be last.
 * Type::Array<Color> values() {
 *     return COLOR_VALUES;
 * }
 *
 * [/code]
 *
 * This class maintains unicity (operator == can be used in place of Object.equals(obj)).
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Enum.html">
 *       Java - Enum</a>
 * @version 1.0
 */
 class javolution::lang::Enum_ANY_API : public virtual javolution::lang::Object_API {

     /**
      * Holds the declaring type of this enum.
      */
     Class_ANY _declaringClass;

     /**
      * Holds the name of this enum.
      */
     String _name; // Static since the enum is static.

     /**
      * Holds the ordinal value (may be duplicated).
      */
     Type::int32 _ordinal;

 	 /**
  	  * Private constructor (class final).
 	  */
     Enum_ANY_API(Class_ANY const& declaringClass, String const& name, Type::int32 ordinal)
     : _declaringClass(declaringClass), _name(name), _ordinal(ordinal) {
     }

 public:

    /**
     * Creates a new enum constant of the specified enum type, name and ordinal
     * value.
     *
     * @param enumType the Class object of the enum type to create.
     * @param name the name of the constant to create.
     * @param ordinal the ordinal value of the constant to create.
     * @return the enum constant of the specified enum type with the specified name and value.
     */
    JAVOLUTION_DLL static Enum_ANY newInstance(Class_ANY const& enumType, String const& name, Type::int32 ordinal);

    /**
     * Creates a new enum constant of the specified enum type and name.
     * The ordinal value is automatically set to the previous enum value plus
     * one (zero if first enum).
     *
     * @param enumType the Class object of the enum type to create.
     * @param name the name of the constant to create.
     * @return the enum constant of the specified enum type with the specified name.
     */
    JAVOLUTION_DLL static Enum_ANY newInstance(Class_ANY const& enumType, String const& name);

    /**
     * Returns an array containing all of the values of the specified enum type
     * in the order they are declared. Enum sub-types may redefine this
     * operation without the enum type parameter (see class description example).
     *
     * @param enumType the class object identifying the enum type.
     * @return the whole enumeration.
     */
    JAVOLUTION_DLL static Type::Array<Enum_ANY> values(Class_ANY const& enumType);

    /**
     * Returns the enum constant of the specified enum type with the specified
     * name.
     *
     * @param enumType the Class object of the enum type from which to return a constant.
     * @param name the name of the constant to return.
     * @return the enum constant of the specified enum type with the specified name.
     * @throws IllegalArgumentException if the specified enum type has no constant
     *         with the specified name.
     */
    JAVOLUTION_DLL static Enum_ANY valueOf(Class_ANY const& enumType, String const& name);

    /**
     * Returns the Class object corresponding to this enum constant's enum type.
     */
    Class_ANY getDeclaringClass() {
        return _declaringClass;
    }

    /**
     * Returns the name of this enum.
     */
    String name() const {
        return _name;
    }

    /**
     * Returns the ordinal value of this enum.
     */
    Type::int32 ordinal() const {
        return _ordinal;
    }

    /**
     * Returns the textual representation of this enum.
     */
    String toString() const {
        return name();
    }

    ///////////////////////////////////////////////////////////////////////////
    // No need to override Object.equals and Object.hashCode due to unicity. //
    ///////////////////////////////////////////////////////////////////////////

 };

/////////////////////////
// Parameterized types //
/////////////////////////

template <class E> class javolution::lang::Enum_API : public Enum_ANY_API  {
    Enum_API() {} // Private constructor. There is no instance of this class.
public:
    static Enum<E> newInstance(Class<E> const& enumType, String const& enumName, Type::int32 ordinalValue) {
        Enum_ANY enumAny = Enum_ANY_API::newInstance(enumType, enumName, ordinalValue);
        return Enum<E>(enumAny);
    }
    static Enum<E> newInstance(Class<E> const& enumType, String const& enumName) {
        Enum_ANY enumAny = Enum_ANY_API::newInstance(enumType, enumName);
        return Enum<E>(enumAny);
    }
    static Type::Array< Enum<E> > values(Class<E> const& enumType) {
        Type::Array<Enum_ANY> enumAnys = Enum_ANY_API::values(enumType);
        Type::int32 length = enumAnys.length;
        Type::Array< Enum<E> > enums =  Type::Array< Enum<E> >(length);
        for (int i = 0; i < length; i++) {
            enums[i] = Enum<E>(enumAnys[i]);
        }
        return enums;
    }
    static Enum<E> valueOf(Class<E> const& enumType, String const& enumName) {
        Enum_ANY enumAny = Enum_ANY_API::valueOf(enumType, enumName);
        return Enum<E>(enumAny);
    }
    Class<E> getDeclaringClass() {
        return Class<E>(Enum_ANY_API::getDeclaringClass());
    }
};

template<class T>
class javolution::lang::Enum : public javolution::lang::Enum_ANY  {
public:
    Enum(Type::NullHandle = Type::Null) : Enum_ANY() {} // Null.
    explicit Enum(Enum_ANY const& source) : Enum_ANY(source) {}
};

#endif
