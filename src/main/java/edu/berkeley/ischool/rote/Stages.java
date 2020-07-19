/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.berkeley.ischool.rote;

import spark.Request;
import spark.Response;

/**
 *
 * @author gunnar
 */
public class Stages {

    public static enum Stage {
        INVALID,
        START,
        INFO,
        CONTENT1,
        CONTENT2_READAGAIN,
        DISTRACTION1,
        TEST1,
        CONTENT2,
        CONTENT2_READ,
        CONTENT2_SPEAK,
        CONTENT2_WRITE,
        DISTRACTION2,
        TEST2,
        RESULTS,
        FINISHED;
    }

    public static String currentStage(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        
        // are we in a plausible stage for this page?
        String possible = req.queryParamOrDefault("stage", "");
        String[] stages = possible.split(",");
        for (String stage : stages) {
            stage = stage.trim();
            if (stage.equals(rs.stage.toString())) {
                return "ok";
            }
        }

        // no, recommend redirects
        String url;
        switch (rs.stage) {
            case START:
                url = "index.html";
                break;
            case INFO:
                url = "stage_info.html";
                break;
            case CONTENT1:
                url = "stage_content.html";
                break;
            case CONTENT2_READAGAIN:
                url = "stage_content.html";
                break;
            case DISTRACTION1:
                url = "stage_distraction.html";
                break;
            case TEST1:
                url = "stage_test.html";
                break;
            case CONTENT2:
                url = "stage_content.html";
                break;
            case CONTENT2_SPEAK:
                url = "stage_speak.html";
                break;
            case CONTENT2_WRITE:
                url = "stage_write.html";
                break;
            case DISTRACTION2:
                url = "stage_distraction.hml";
                break;
            case TEST2:
                url = "stage_test.html";
                break;
            case FINISHED:
                url = "stage_finished.html";
                break;
            case INVALID:
            default:
                url = "index.html";
        }
        System.out.println("Wrong stage, redirecting to: "+url);
        return url;
    }

    public static String start(Request req, Response res) {
        // startSession will assign treat/control and establish the context
        Boolean success = RoteSession.startSession(req);
        RoteSession rs = RoteSession.getSession(req);
        if (success) {
            rs.stage = Stage.INFO;
            res.redirect("stage_info.html");
        } else {
            rs.stage = Stage.INFO;
            res.redirect("stage_info.html");
//            res.redirect("error.html?reason=sessionexists");
//            rs.stage = Main.NextStage.INVALID;
        }

        return "started";
    }

    public static String postInfo(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        String age = req.queryParamOrDefault("Age", "NA");
        String gender = req.queryParamOrDefault("Gender", "NA");
        String practice = req.queryParamOrDefault("Practice", "NA");
        String reading = req.queryParamOrDefault("Reading", "NA");

        rs.assignTreatControl();

//        System.out.println(age);
//        System.out.println(gender);
//        System.out.println(knowledge);
//        System.out.println(reading);
        Main.logCov(rs + "," + age + "," + gender + "," + practice + "," + reading);

        res.redirect("stage_content");
        return "ok";
    }

    public static String content(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        if (rs.stage == Stage.INFO) {
            rs.stage = Stage.CONTENT1;
        } else if (rs.stage == Stage.TEST1) {
            rs.stage = Stage.CONTENT2;
        } else {
            System.out.println("Invalid stage in 'content': " + rs.stage);
            return "";
        }

        System.out.println("content: next stage is " + rs.stage);
        res.redirect("before_content.html");
        return "";
    }

    public static String continueContent(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        if (rs.stage == Stage.CONTENT1) {
            System.out.println("After content1, redirecting to distraction");
            res.redirect("stage_distraction");
            return "";
        }

        // stage 2, decide between treatent and control
        if (rs.getTreatment()) {
            // treat
            System.out.println("After content2 in TREATMENT, redirecting to speak");
            res.redirect("stage_speak");
            return "";
        } else {
            // control
            if (rs.stage == Stage.CONTENT2_READAGAIN) {
                System.out.println("After content2 in CONTROL, redirecting to disctraction");
                res.redirect("stage_distraction");
            } else {
                System.out.println("After content2 in CONTROL, redirecting to readagain");
                res.redirect("stage_readagain");
            }
            return "";
        }
    }

    public static String readAgain(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        rs.stage = Stage.CONTENT2_READAGAIN;
        res.redirect("before_readagain.html");
        return "";
    }

    public static String speak(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        rs.stage = Stage.CONTENT2_SPEAK;
        res.redirect("before_speak.html");
        return "";
    }

    public static String write(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        rs.stage = Stage.CONTENT2_WRITE;
        res.redirect("before_write.html");
        return "";
    }

    public static String postWrite(Request req, Response res) {
        String n1 = req.queryParamOrDefault("N1", "NA");
        String n2 = req.queryParamOrDefault("N2", "NA");
        String n3 = req.queryParamOrDefault("N3", "NA");
        String n4 = req.queryParamOrDefault("N4", "NA");

        Main.log(req, RoteSession.getSession(req) + ": Written answers: [" + n1 + "] [" + n2 + "] [" + n3 + "] [" + n4 + "]");

        res.redirect("stage_distraction");
        return "";
    }

    public static String distraction(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        if (rs.stage == Stage.CONTENT1) {
            rs.stage = Stage.DISTRACTION1;
            res.redirect("before_distraction1.html");
        } else {
            rs.stage = Stage.DISTRACTION2;
            res.redirect("before_distraction2.html");
        }
        return "";
    }

    public static String test(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        System.out.println("In test, stage is " + rs.stage);
        if (rs.stage == Stage.DISTRACTION1) {
            rs.stage = Stage.TEST1;
        } else {
            rs.stage = Stage.TEST2;
        }
        res.redirect("before_test.html");
        return "";
    }

    public static String postTest(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);

        String q1 = req.queryParamOrDefault("Q1", "NA");
        String q2 = req.queryParamOrDefault("Q2", "NA");
        String q3 = req.queryParamOrDefault("Q3", "NA");
        String q4 = req.queryParamOrDefault("Q4", "NA");

        Main.logTest(rs + "," + q1 + "," + q2 + "," + q3 + "," + q4);

        if (rs.stage == Stage.TEST1) {
            res.redirect("stage_content");
        } else {
            res.redirect("stage_finished");
        }
        return "ok";
    }

    public static String finished(Request req, Response res) {
        RoteSession rs = RoteSession.getSession(req);
        rs.stage = Stage.FINISHED;
        res.redirect("stage_finished.html");
        return "";
    }
}
