package com.company.controller;

import com.company.controller.collectors.DBCollector;
import com.company.controller.collectors.PostgresCollector;
import com.company.controller.types.DatabaseType;
import com.company.controller.util.FileUtil;
import com.company.controller.util.JSONUtil;
import com.company.controller.util.json.JSONException;
import com.company.controller.util.json.JSONObject;
import com.company.controller.util.json.Test;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import sun.misc.Signal;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static final Logger LOG = Logger.getLogger(Main.class);

    //默认的输出目录名
    private static final String DEFAULT_DIRECTORY = "output";

    //默认的观察时间间隔(-1 means 5 min)
    private static final int DEFAULT_TIME_SECONDS = -1;

    // Path to JSON schema directory
    private static final String SCHEMA_PATH = "src/main/java/com/controller/json_validation_schema";

    private static final int TO_MILLISECONDS = 1000;

    private static boolean keepRunning = true;

    public static void main(String[] args) {
	    // write your code here
        // Initialize log4j  log4j
        // 要放到项目的最外层根目录下才有效
        PropertyConfigurator.configure("log4j.properties");

        // Initialize keepRunning
        keepRunning = true;

        //自定义了java命令行的参数解析 Posix风格的解析器
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("c", "config", true, "【配置文件必须】 Controller configuration file");
        options.addOption("t", "time", true,"【观察阶段的时间】in seconds, default is 300s");
        options.addOption("d", "directory", true, "【结果文件输出目录】, default is output");
        options.addOption("h", "help", true, "【帮助】");


        //解析命令行参数
        CommandLine argsLine;
        try{
            argsLine = parser.parse(options, args);
        }
        catch(ParseException e){
            LOG.error("异常——无法解析命令行参数");
            printUsage(options);
            return;
        }

        //输入help，检查有没有配置文件
        if(argsLine.hasOption("h")){
            printUsage(options);
            return;
        }
        else if(argsLine.hasOption("c") == false){
            LOG.error("错误——缺少controller的配置文件");
            printUsage(options);
            return;
        }

        //解析时间
        int time = DEFAULT_TIME_SECONDS;
        if(argsLine.hasOption("t")){
            time = Integer.parseInt(argsLine.getOptionValue("t"));
        }
        LOG.info("观察阶段的时间被设置为：" + time + "/秒。");

        //解析目录
        String outputDirectory = DEFAULT_DIRECTORY;
        if(argsLine.hasOption("d")){
            outputDirectory = argsLine.getOptionValue("d");
        }
        LOG.info("结果输出目录为：" + outputDirectory);
        FileUtil.makeDirIfNotExists(outputDirectory);


        //解析配置文件,这边是肯定存在的了
        String configPath = argsLine.getOptionValue("c");
        File configFile = new File(configPath);
        System.out.println(configFile);


        //检查配置文件格式
        /*
         *  默认json格式正确
         */

        ControllerConfiguration config = null;
        try{
            JSONObject input = new JSONObject(FileUtil.readFile(configFile));
            config = new ControllerConfiguration(
                    input.getString("database_type"), input.getString("username"),
                    input.getString("password"), input.getString("database_url"),
                    input.getString("upload_code"), input.getString("upload_url"),
                    input.getString("workload_name"));
            //System.out.println(config.getDBURL()); 解析成功
        }
        catch (JSONException e){
            LOG.error("配置文件json解析失败");
            e.printStackTrace();
        }


        DBCollector collector = getCollector(config);



        try{
            //查询之前的第一次连接
            LOG.info("查询之前的第一次连接");
            String metricsBefore = collector.collectMetrics();
            /*
             * 输出的metricsBefore的json格式的检查
             */

            PrintWriter metricsWriter =
                    new PrintWriter(FileUtil.joinPath(outputDirectory, "metrics_before.json"), "UTF-8");
            metricsWriter.println(metricsBefore);
            metricsWriter.close();


            String knobs = collector.collectParameters();
            /*
             * 输出的节点信息json格式的检查
             */

            PrintWriter konbsWriter =
                    new PrintWriter(FileUtil.joinPath(outputDirectory, "knobs.json"), "UTF-8");
            konbsWriter.println(knobs);
            konbsWriter.close();

            //添加了一个信号量的处理
            Signal.handle(new Signal("INT"), signal -> keepRunning = false);
            File f = new File("pid.txt");

            //获取当前进程的pid，在开始工作流之前，将pid写入到文件
            if(time<0){
                //jvm中的单例
                String vmName = ManagementFactory.getRuntimeMXBean().getName();

                /*
                 * String vmName = ManagementFactory.getRuntimeMXBean().getVmName();
                 * System.out.println(Name+" ??? "+VmName);
                 * 一个是进程的信息，一个VM的信息
                 */
                int pos = vmName.indexOf("@");
                int pid = Integer.valueOf(vmName.substring(0, pos));
                try {
                    f.createNewFile();
                    PrintWriter pidWriter = new PrintWriter(f);
                    pidWriter.println(pid);
                    pidWriter.flush();   //立即写入，println我觉的也会立即刷入
                    pidWriter.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }

            //记录开始时间
            long startTime = System.currentTimeMillis();
            LOG.info("开始实验...");

            //休眠
            if(time>=0){
                Thread.sleep(time * TO_MILLISECONDS);
            }
            else{
                while(keepRunning){      //如果是信号量控制的中断
                    Thread.sleep(1);
                }
                f.delete();
                //删除该文件
            }

            long endTime = System.currentTimeMillis();
            long observationTime = (time >= 0) ? time : (endTime - startTime) / TO_MILLISECONDS;
            LOG.info("结束实验...");


            //概要json的构建
            JSONObject summary = null;
            try {
                summary = new JSONObject();
                summary.put("start_time", startTime);
                summary.put("end_time", endTime);
                summary.put("observation_time", observationTime);
                summary.put("database_type", config.getDBName());
                summary.put("database_version", collector.collectVersion());
                summary.put("workload_name", config.getWorkloadName());
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            /*
             * summary.toString json的检查 略
             */

            //将summary JSONOBJECT 写到JSON文件中
            PrintWriter summaryout = new PrintWriter(FileUtil.joinPath(outputDirectory, "summary.json"), "UTF-8");
            summaryout.println(JSONUtil.format(summary.toString()));
            summaryout.close();

            //workload执行完之后的第二次收集
            LOG.info("workload执行完之后的第二次收集...");
            if(collector==null){
                collector = getCollector(config);
            }
            else{
                System.out.println("collector 可用");
            }
            String metricsAfter = collector.collectMetrics();
            /*
             * metricsAfter json检查
             */

            PrintWriter metricsWriterFinal =
                    new PrintWriter(FileUtil.joinPath(outputDirectory,"metrics_after.json"),"UTF-8");
            metricsWriterFinal.println(metricsAfter);
            metricsWriterFinal.close();



        }
        catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
            LOG.error("Failed to produce output files");
            e.printStackTrace();
        }


        //保证上传地址有效
        if(config.getUploadURL()!=null && !config.getUploadURL().equals("")){
            Map<String, String> outfiles = new HashMap<>();
            outfiles.put("knobs", FileUtil.joinPath(outputDirectory, "knobs.json"));
            outfiles.put("metrics_before", FileUtil.joinPath(outputDirectory, "metrics_before.json"));
            outfiles.put("metrics_after", FileUtil.joinPath(outputDirectory, "metrics_after.json"));
            outfiles.put("summary", FileUtil.joinPath(outputDirectory, "summary.json"));
            ResultUploader.upload(config.getUploadURL(), config.getUploadCode(), outfiles);
        }
        else{
            LOG.warn("没有上传地址，跳过上传...");
        }

    }



    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("controller", options);
    }


    private static DBCollector getCollector(ControllerConfiguration config) {
        DBCollector collector = null;
        switch (config.getDBType()) {
            case POSTGRES:
                System.out.println(config.getDBURL()+" "+config.getDBUsername()+" "+config.getDBPassword());
                collector =
                        new PostgresCollector(
                                config.getDBURL(), config.getDBUsername(), config.getDBPassword());
                break;
            case MYSQL:
                collector = null;       //do something later
                break;
            case SAPHANA:
                collector = null;       //do something later
                break;
            default:
                LOG.error("Invalid database type");
                throw new RuntimeException("Invalid database type");
        }
        return collector;
    }
}
