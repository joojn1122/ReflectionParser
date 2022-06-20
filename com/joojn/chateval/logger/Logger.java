package com.joojn.chateval.logger;


public class Logger {

//    public static void error(String s)
//    {
//        error(s, Bukkit.getConsoleSender());
//    }
//
//    public static void error(String s, CommandSender sender)
//    {
//        if(sender == null) System.out.println(String.format("[ERROR] %s ", s));
//        else Bukkit.getConsoleSender().sendMessage((String.format(ChatColor.RED + "[ERROR] %s ", s)));
//    }
//
//    public static void info(String s)
//    {
//        Bukkit.getConsoleSender().sendMessage((String.format("§f[INFO] %s ", s)));
//    }

    public static void error(String s)
    {
        System.out.println("[ERROR]: " + s);
    }

    public static void info(String s)
    {
        System.out.println(String.format("§f[INFO] %s ", s));
    }
}
