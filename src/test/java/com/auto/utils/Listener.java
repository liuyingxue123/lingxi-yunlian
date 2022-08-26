package com.auto.utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;

public class Listener implements ITestListener {
    @Override
    public void onTestStart(ITestResult iTestResult) {

    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {

        String result ="【自动化运行失败详情】\n";
        result = result + "Class : " + iTestResult.getInstanceName() + "\n";
        result = result + "Method : " + iTestResult.getName() + "\n";
        result = result + "ERROR message : " + iTestResult.getThrowable() + "\n";

        System.out.println("debug message :" + result);

        try {
            DingTalk.sendMessageDingding(result.replace("\"","\\\""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }


    @Override
    public void onFinish(ITestContext iTestContext) {

    }
}
