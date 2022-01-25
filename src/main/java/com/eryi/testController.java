package com.eryi;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @description:
 * @author: ZouJiaNan
 * @date: 2022/1/24 15:29
 */
@RestController
public class testController {

    private Set<JarEntry> parentdepJars=new HashSet<>();

    @RequestMapping("/test")
    public void test(){
        try {
            //1.获取当前改成打包出的jar包
            String parentJarPath=this.getClass().getClassLoader().getResource("").getPath().split("!")[0];
            if(parentJarPath.startsWith("file:/")){
                parentJarPath=parentJarPath.replaceAll("file:/","");
            }
            JarFile jar=new JarFile(parentJarPath);
            //2.遍历当前jar包中所有资源，获得依赖的jar包列表
            Enumeration<JarEntry> parentEntries=jar.entries();
            while(parentEntries.hasMoreElements()){
                JarEntry jarEntryTemp1=parentEntries.nextElement();
                if(jarEntryTemp1.getName().endsWith(".jar")){
                    if(parentdepJars.contains(jarEntryTemp1.getName())){
                        System.out.println("jar is exist:" + jarEntryTemp1.getName());
                        continue;
                    }
                    parentdepJars.add(jarEntryTemp1);
                }
            }

            //3.遍历当前jar包的jar依赖列表
            for (JarEntry jarEntry:parentdepJars){
                JarFile jarEntryTemp2=new JarFile(jarEntry.getName());
                Enumeration<JarEntry> childEntries=jarEntryTemp2.entries();
                while(childEntries.hasMoreElements()) {
                    JarEntry jarEntry1=childEntries.nextElement();
                    if (jarEntry1.getName().endsWith("pom.properties")) {
                        Properties properties = new Properties();
                        properties.load(this.getClass().getResourceAsStream("/" + jarEntry1.getName()));
                        Enumeration propertyNames = properties.propertyNames();
                        while (propertyNames.hasMoreElements()) {
                            System.out.println(properties.get(propertyNames.nextElement()));
                        }
                    }
                }
            }
//            for (String jarName:jars) {
//                System.out.println(jarName);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            File file =new File(this.getClass().getClassLoader().getResource("/lib").getPath());
//            File[] files=file.listFiles();
//            for (File f:files) {
//                JarFile jarFile=new JarFile(f.getCanonicalPath());
//                System.out.println(jarFile.getName());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Enumeration<JarEntry> jarEntrys=jarFile.entries();
//        while (jarEntrys.hasMoreElements()){
//            JarEntry jarEntry=jarEntrys.nextElement();
//            if (jarEntry.getName().endsWith("pom.properties")){
//                Properties properties=new Properties();
//                properties.load(test.class.getResourceAsStream("/"+jarEntry.getName()));
//                Enumeration propertyNames=properties.propertyNames();
//                while(propertyNames.hasMoreElements()){
//                    System.out.println(properties.get(propertyNames.nextElement()));
//                }
//            }
////        }
    }
}
