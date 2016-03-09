/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.tools.simulator.bootstrap;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.mobicents.protocols.ss7.tools.simulator.MainCore;
import org.mobicents.protocols.ss7.tools.simulatorgui.MainGui;

/**
 * @author <a href="mailto:amit.bhayani@jboss.com">amit bhayani</a>
 */
public class Main {

    private static final String APP_NAME = "SS7 Simulator";

    private static final String HOME_DIR = "SIMULATOR_HOME";
    private static final String LOG4J_URL = "/conf/log4j.properties";
    private static final String LOG4J_URL_XML = "/conf/log4j.xml";
    public static final String SIMULATOR_HOME = "simulator.home.dir";
    public static final String SIMULATOR_DATA = "simulator.data.dir";
    private static int index = 0;

    private static Logger logger = Logger.getLogger(Main.class);

    private String command = null;
    private String appName = "main";
    private int rmiPort = -1;
    private int rmiPort2 = -1;
    private int httpPort = -1;
    private String attack_command = null;
    private String simple_attack_goal = null;
    private int complexNumSubs = 0;
    private int chanceOfAttack = 0;

    public static void main(String[] args) throws Throwable {
        String homeDir = getHomeDir(args);
        System.setProperty(SIMULATOR_HOME, homeDir);
        System.setProperty(SIMULATOR_DATA, homeDir + File.separator + "data" + File.separator);

        if (!initLOG4JProperties(homeDir) && !initLOG4JXml(homeDir)) {
            logger.error("Failed to initialize loggin, no configuration. Defaults are used.");
        }

        logger.info("log4j configured");

        Main main = new Main();

        main.processCommandLine(args);
        main.boot();
    }

    private void processCommandLine(String[] args) {

        String programName = System.getProperty("program.name", APP_NAME);

        int c;
        String arg;
        LongOpt[] longopts = new LongOpt[6];
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("name", LongOpt.REQUIRED_ARGUMENT, null, 'n');
        longopts[2] = new LongOpt("http", LongOpt.REQUIRED_ARGUMENT, null, 't');
        longopts[3] = new LongOpt("rmi", LongOpt.REQUIRED_ARGUMENT, null, 'r');
        longopts[4] = new LongOpt("core", LongOpt.NO_ARGUMENT, null, 0);
        longopts[5] = new LongOpt("attack_simulation", LongOpt.REQUIRED_ARGUMENT, null, 0);

        Getopt g = new Getopt(APP_NAME, args, "-:n:t:r:h:a:c:s:m:", longopts);
        g.setOpterr(false); // We'll do our own error handling
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {

                case 't':
                    // http port
                    arg = g.getOptarg();
                    this.httpPort = Integer.parseInt(arg);
                    if (this.httpPort < 0 || this.httpPort > 65000) {
                        System.err.println("Http port should be in range 0 to 65000");
                        System.exit(0);
                    }
                    break;
                case 'r':
                    // rmi port
                    arg = g.getOptarg();
                    String[] ss = arg.split(",");
                    this.rmiPort = Integer.parseInt(ss[0]);
                    if (this.rmiPort < 0 || this.rmiPort > 65000) {
                        System.err.println("RMI port should be in range 0 to 65000");
                        System.exit(0);
                    }
                    if (ss.length >= 2) {
                        this.rmiPort2 = Integer.parseInt(ss[1]);
                        if (this.rmiPort2 < 0 || this.rmiPort2 > 65000) {
                            System.err.println("RMI port 2 should be in range 0 to 65000");
                            System.exit(0);
                        }
                    }
                    break;
                case 'n':
                    // name
                    arg = g.getOptarg();
                    this.appName = arg;
                    break;

                case 'h':
                    this.genericHelp();
                    break;

                case ':':
                    System.out.println("You need an argument for option " + (char) g.getOptopt());
                    System.exit(0);
                    break;
                case '?':
                    System.out.println("The option '" + (char) g.getOptopt() + "' is not valid");
                    System.exit(0);
                    break;
                case 'a':
                    //Attack simulation type
                    arg = g.getOptarg();
                    if (arg.equals("simple")) {
                        this.attack_command = "simple";
                    } else if (arg.equals("complex")){
                        this.attack_command = "complex";
                    } else {
                        System.out.println("Invalid command " + arg);
                        this.genericHelp();
                    }
                    break;
                case 'c':
                    //Change of generating an attack.
                    arg = g.getOptarg();
                    this.chanceOfAttack = Integer.valueOf(arg);
                    break;
                case 'm':
                    //Attack goal to perform, in cooperation with -a simple
                    arg = g.getOptarg();
                    this.simple_attack_goal = arg;
                    break;
                case 's':
                    //Number of subscribers to generate in complex attack simulation.
                    arg = g.getOptarg();
                    this.complexNumSubs = Integer.valueOf(arg);
                    break;
                case 1:
                    String optArg = g.getOptarg();
                    if (optArg.equals("core")) {
                        this.command = "core";
                    } else if (optArg.equals("gui")) {
                        this.command = "gui";
                    } else if (optArg.equals("attack_simulation")) {
                        this.command = "attack_simulation";
                    } else if (optArg.equals("help")) {
                        if (this.command == null) {
                            this.genericHelp();
                        } else if (this.command.equals("core")) {
                            this.coreHelp();
                        } else if (this.command.equals("gui")) {
                            this.guiHelp();
                        } else if (this.command.equals("attack_simulation")){
                            this.attackHelp();
                        } else {
                            System.out.println("Invalid command " + optArg);
                            this.genericHelp();
                        }
                    } else {
                        System.out.println("Invalid command " + optArg);
                        this.genericHelp();
                    }
                    break;

                default:
                    this.genericHelp();
                    break;
            }
        }

    }

    private void genericHelp() {
        System.out.println("usage: " + APP_NAME + "<command> [options]");
        System.out.println();
        System.out.println("command:");
        System.out.println("    core      Start the SS7 simulator core");
        System.out.println("    gui       Start the SS7 simulator gui");
        System.out.println("    attack_simulation Start the SS7 attack simulator");
        System.out.println();
        System.out.println("see 'run <command> help' for more information on a specific command:");
        System.out.println();
        System.exit(0);
    }

    private void coreHelp() {
        System.out.println("core: Starts the simulator core");
        System.out.println();
        System.out.println("usage: " + APP_NAME + " core [options]");
        System.out.println();
        System.out.println("options:");
        System.out.println("    -n, --name=<simulator name>     Simulator name. If not passed default is main");
        System.out.println("    -t, --http=<http port>          Http port for core");
        System.out.println("    -r, --rmi=<rmi port>            RMI port for core");
        System.out.println();
        System.exit(0);
    }

    private void guiHelp() {
        System.out.println("gui: Starts the simulator gui");
        System.out.println();
        System.out.println("usage: " + APP_NAME + " gui [options]");
        System.out.println();
        System.out.println("options:");
        System.out.println("    -n, --name=<simulator name>   Simulator name. If not passed default is main");
        System.out.println();
        System.exit(0);
    }

    private void attackHelp() {
        System.out.println("attack_simulation: Starts the attack simulator");
        System.out.println();
        System.out.println("usage: " + APP_NAME + " attack_simulation -a [simple/complex] [options]");
        System.out.println();
        System.out.println("options:");
        System.out.println("    -a, Attack simulation type used. Can either be simple or complex.");
        System.out.println("    -c, Distribution of attacks in simulated traffic. Integer from 0-50");
        System.out.println("    -m, Simple attack simulation goal. Specifies which attack should be launched. Can only be used with -a simple.");
        System.out.println("    -s, Specifies the number of subscribers that should be simulated as connected to the network. Can only be used with -a complex.");
        System.out.println();
        System.exit(0);
    }

    private static boolean initLOG4JProperties(String homeDir) {
        String Log4jURL = homeDir + LOG4J_URL;

        try {
            URL log4jurl = getURL(Log4jURL);
            InputStream inStreamLog4j = log4jurl.openStream();
            Properties propertiesLog4j = new Properties();
            try {
                propertiesLog4j.load(inStreamLog4j);
                PropertyConfigurator.configure(propertiesLog4j);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("Failed to initialize LOG4J with properties file.");
            return false;
        }
        return true;
    }

    private static boolean initLOG4JXml(String homeDir) {
        String Log4jURL = homeDir + LOG4J_URL_XML;

        try {
            URL log4jurl = getURL(Log4jURL);
            DOMConfigurator.configure(log4jurl);
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("Failed to initialize LOG4J with xml file.");
            return false;
        }
        return true;
    }

    /**
     * Gets the Media Server Home directory.
     *
     * @param args the command line arguments
     * @return the path to the home directory.
     */
    private static String getHomeDir(String[] args) {
        if (System.getenv(HOME_DIR) == null) {
            if (args.length > index) {
                return args[index++];
            } else {
                return ".";
            }
        } else {
            return System.getenv(HOME_DIR);
        }
    }

    protected void boot() throws Throwable {
        if (this.command == null) {
            System.out.println("No command passed");
            this.genericHelp();
        } else if (this.command.equals("gui")) {
            EventQueue.invokeLater(new MainGui(appName));
        } else if (this.command.equals("core")) {
            MainCore mainCore = new MainCore();
            mainCore.start(appName, httpPort, rmiPort, rmiPort2);
        } else if (this.command.equals("attack_simulation")) {
            MainCore mainCore = new MainCore();
            if (this.attack_command.equals("simple")) {
                if(this.simple_attack_goal != null && !this.simple_attack_goal.isEmpty()) {
                    mainCore.startAttackSimulation(true, simple_attack_goal, 0, 0);
                } else {
                    System.out.println("Error: Option m not specified.");
                    this.attackHelp();
                }
            } else if (this.attack_command.equals("complex")) {
                if (this.complexNumSubs > 0) {
                    mainCore.startAttackSimulation(false, null, this.complexNumSubs, this.chanceOfAttack);
                } else if (this.chanceOfAttack < 0 || this.chanceOfAttack > 100){
                    mainCore.startAttackSimulation(false, null, this.complexNumSubs, this.chanceOfAttack);
                }else {
                    System.out.println("Error: Option s not specified.");
                    this.attackHelp();
                }
            }
        }
    }

    public static URL getURL(String url) throws Exception {
        File file = new File(url);
        if (file.exists() == false) {
            throw new IllegalArgumentException("No such file: " + url);
        }
        return file.toURI().toURL();
    }

    protected void registerShutdownThread() {
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownThread()));
    }

    private class ShutdownThread implements Runnable {

        public void run() {
            System.out.println("Shutting down");

        }
    }
}
