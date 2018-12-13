package com.github.bkm016.bsteams.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.bkm016.bsteams.command.enums.CommandType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlayerCommand {
	
	/**
	 * 指令名
	 * 
	 * @return
	 */
	String cmd();
	
	/**
	 * 指令参数
	 * 
	 * @return
	 */
	String arg() default "";
	
	/**
	 * 指令权限
	 * 
	 * @return
	 */
	String permission() default "bsteams.use";
	
	/**
	 * 是否在列表中隐藏
	 * 
	 * @return
	 */
	boolean hide() default false;
	
	/**
	 * 指令类型
	 * 
	 * @return
	 */
	CommandType[] type() default CommandType.ALL;
}
