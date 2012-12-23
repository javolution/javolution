/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import javolution.context.AbstractContext;
import javolution.context.FormatContext;
import static javolution.internal.osgi.JavolutionActivator.TEXT_CONTEXT_TRACKER;
import javolution.lang.Configurable;

/**
 * <p> This abstract class represents the current context for plain text 
 *     parsing/formatting.</p>
 * 
 * <p> As a minimum, implementations of this class should provide a {@link TextFormat} 
 *     for the following classes:
 *     <code><ul>
 *       <li>java.lang.String</li>
 *       <li>java.lang.Boolean</li>
 *       <li>java.lang.Character</li>
 *       <li>java.lang.Byte</li>
 *       <li>java.lang.Short</li>
 *       <li>java.lang.Integer</li>
 *       <li>java.lang.Long</li>
 *       <li>java.lang.Float</li>
 *       <li>java.lang.Double</li>
 *       <li>java.lang.Class</li>
 *       <li>java.lang.Object - formatting returns Object.toString(), parsing not supported</li> 
 *     </ul>></code>
 *     Implementations should be able to use reflection to retrieve
 *     the text format for classes annotated with the
 *     {@link javolution.annotation.Format} tag.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class TextContext extends FormatContext<TextContext> {

   /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable(false) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseBoolean(configuration));
        }
    };
 
    /**
     * Default constructor.
     */
    protected TextContext() {
    }
    
  /**
     * Enters a new text context instance.
     * 
     * @return the new text context implementation entered.
     */
    public static TextContext enter() {
        TextContext ctx = AbstractContext.current(TextContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return TEXT_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault()).inner().enterScope();
    }
    
    /**
     * Returns the text format for the specified class.
     * 
     * @param cls the class for which the text format is returned.
     * @throws UnsupportedOperationException if the format for the specified class 
     *         does not exist.
     */
     public static <T> TextFormat<T> getFormat(Class<T> cls) throws UnsupportedOperationException {
         TextContext ctx = AbstractContext.current(TextContext.class);
         if (ctx != null) {
            ctx = TEXT_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
         }
         return ctx.findFormat(cls);
     }  
     
    /**
     * Sets the format for the specified class.
     * 
     * @param format the format associated to the specified class.
     * @param cls the class for which the text format is set.
     */
    protected abstract <T> void setFormat(TextFormat<T> format, Class<T> cls);    
  
    /**
     * Searches the format for the specified class. If this context has no
     * format for the specified class, it uses the 
     * {@link javolution.annotation.Format Format} annotation to retrieve 
     * the text format associated with the class.
     * If there is no format or the format found is an instance of 
     * {@link javolution.annotation.Format.UnsupportedTextFormat} 
     * then an exception is raised.
     * 
     * @param cls the class for which the text format is returned.
     * @throws UnsupportedOperationException if the format for the specified class 
     *         does not exist.
     */
    protected abstract <T> TextFormat<T> findFormat(Class<T> cls) throws UnsupportedOperationException;

    /**
     * Exits the scope of this text context; reverts to the text formats 
     * defined before this context was entered.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() throws IllegalStateException {
        super.exit();
    }    
}