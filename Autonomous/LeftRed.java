package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
/*
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
*/
import java.util.ArrayList;
import java.util.List;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;


import static org.firstinspires.ftc.teamcode.ConceptTensorFlowObjectDetectionWebcam.LABEL_FIRST_ELEMENT;
import static org.firstinspires.ftc.teamcode.ConceptTensorFlowObjectDetectionWebcam.LABEL_SECOND_ELEMENT;
import static org.firstinspires.ftc.teamcode.ConceptTensorFlowObjectDetectionWebcam.TFOD_MODEL_ASSET;
//import static org.firstinspires.ftc.teamcode.WebcamTest.VUFORIA_KEY;

@Autonomous(name="LeftRed", group="Pushbot")

public class LeftRed extends LinearOpMode {

    private static int valQUAD = -1;
    private static int valSingle = -1;
    private static int valZero = -1;

    private static float offsetX = .75f / 8f;//changing this moves the three rects and the three circles left or right, range : (-2, 2) not inclusive
    private static float offsetY = 1.5f / 8f;//changing this moves the three rects and circles up or down, range: (-4, 4) not inclusive

    private static float[] midPos = {4f / 8f + offsetX, 4f / 8f + offsetY};//0 = col, 1 = row
    private static float[] leftPos = {2f / 8f + offsetX, 4f / 8f + offsetY};
    private static float[] rightPos = {6f / 8f + offsetX, 4f / 8f + offsetY};


    //DRIVE, IMU, AND ACCEL CONSTANTS

    BNO055IMU imu;
    Orientation lastAngles = new Orientation();
    double globalAngle, power = .50, correction, rotation;
    PIDController pidRotate, pidDrive;
    HardwarePushbot robot = new HardwarePushbot();
    private ElapsedTime runtime = new ElapsedTime();
    static final double COUNTS_PER_MOTOR_REV = 1120;    //ANDYMARK Motor Encoder ticks
    static final double DRIVE_GEAR_REDUCTION = 1.0;
    static final double WHEEL_DIAMETER_INCHES = 4.0;
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    static double DRIVE_SPEED = 0.9;
    static final double TURN_SPEED = 0.575;
    LogisticFunction function;


    double servoStartingPosition = 0.5;

    double distanceBetweenBlocks = 9.5;


    //boolean center;

    boolean left;

    boolean right;


    boolean targetVisible;

    double blockPosition;

    boolean values[] = new boolean[3];

    String positionArray[] = null;

    public void runOpMode() throws InterruptedException {

        teleUpdate("status", "Starting runOpMode");
        robot.init(hardwareMap);
        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //robot.FoundationMoverLeft.setPosition(0.3);    //Pull Position 0.75
        //robot.FoundationMoverRight.setPosition(0.88);
        function = new LogisticFunction(0.6);
        teleUpdate("status", "Starting runOpMode");
        robot.init(hardwareMap);
        robot.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //robot.FoundationMoverLeft.setPosition(0.3);    //Pull Position 0.75
        //robot.FoundationMoverRight.setPosition(0.88);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;
        teleUpdate("WWWWWWWWWWWWWWWWWWWWWWWWWWWWW", "");
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        teleUpdate("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEee", "");
        imu.initialize(parameters);
        teleUpdate("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQqqq", "");
        pidRotate = new PIDController(.003, .00003, 0);
        teleUpdate("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "");
        pidDrive = new PIDController(.05, 0, 0);

        telemetry.addData("Mode", "calibrating...");
        telemetry.update();
        while (!isStopRequested() && !imu.isGyroCalibrated()) {
            sleep(50);
            idle();
        }
        telemetry.addData("Mode", "waiting for start");
        telemetry.update();

        while (!opModeIsActive()) {

        }
        waitForStart();
semiTurn("counterclockwise",10 );
        encoderDrive(-6, 1, "strafe");
        encoderDrive(-58, 1, "drive");
        robot.shooterMotor.setPower(0.58);
        Thread.sleep(2000);
        robot.shooterServo.setPosition(0);
        Thread.sleep(500);
        robot.shooterServo.setPosition(1);
        robot.shooterMotor.setPower(0.65);
        semiTurn("counterclockwise",2);
        robot.shooterServo.setPosition(0);
        Thread.sleep(500);
        robot.shooterServo.setPosition(1);
        semiTurn("counterclockwise",2);
        robot.shooterServo.setPosition(0);
        Thread.sleep(500);
        robot.shooterServo.setPosition(1);
        Thread.sleep(500);
        encoderDrive(-8, 1, "drive");
        robot.shooterServo.setPosition(0);

    }

    public void blockServoControlLeft(boolean control) {
        if (control) {

            //robot.FoundationMoverLeft.setPosition(0.8);    //Pull Position 0.75
        } else {
            //robot.FoundationMoverLeft.setPosition(0.3);    //Pull Position 0.75
        }

    }

    public void blockServoControlRight(boolean control) {
        if (control) {

            //robot.FoundationMoverRight.setPosition(0.2);    //Pull Position 0.75
        } else {
            //robot.FoundationMoverRight.setPosition(0.88);    //Pull Position 0.75
        }

    }

    public void fullTurn(String type) {
        resetAngle();
        if (type.equals("counterclockwise")) {
            long time = System.currentTimeMillis();
            while (getAngle() <= 82 & (System.currentTimeMillis() < (time + 6000))) {
                power = (.75 * 2 * 0.684 / 5.063) * (-Math.pow((((getAngle()) + 2.9) / 37.4), 2) + 4.5 * ((getAngle() + 2.9) / 37.4)) + 0.159;
                telemetry.addLine("power: " + power);
                telemetry.addLine("angle: " + getAngle());
                telemetry.update();
                robot.frontLeft.setPower(power);
                robot.frontRight.setPower(-power);
                robot.backRight.setPower(-power);
                robot.backLeft.setPower(power);
            }
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.backRight.setPower(0);
            robot.backLeft.setPower(0);
        }
        if (type.equals("clockwise")) {
            long time = System.currentTimeMillis();

            while (getAngle() >= -180 && (System.currentTimeMillis() < (time + 3000))) {
                power = (.75 * 2 * 0.684 / 5.063) * (-Math.pow((((-getAngle()) + 2.9) / 37.4), 2) + 4.5 * ((-getAngle() + 2.9) / 37.4)) + 0.159;
                telemetry.addLine("" + power);
                telemetry.addLine("" + getAngle());
                telemetry.update();
                robot.frontLeft.setPower(-power);
                robot.frontRight.setPower(power);
                robot.backRight.setPower(power);
                robot.backLeft.setPower(-power);
            }
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.backRight.setPower(0);
            robot.backLeft.setPower(0);
        }
        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void semiTurn(String type, int angle) {
        resetAngle();
        if (type.equals("counterclockwise")) {
            long time = System.currentTimeMillis();
            while (getAngle() <= angle & (System.currentTimeMillis() < (time + 6000))) {
                power = (.75 * 2 * 0.684 / 5.063) * (-Math.pow((((getAngle()) + 2.9) / 37.4), 2) + 4.5 * ((getAngle() + 2.9) / 37.4)) + 0.159;
                telemetry.addLine("power: " + power);
                telemetry.addLine("angle: " + getAngle());
                telemetry.update();
                robot.frontLeft.setPower(power);
                robot.frontRight.setPower(-power);
                robot.backRight.setPower(-power);
                robot.backLeft.setPower(power);
            }
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.backRight.setPower(0);
            robot.backLeft.setPower(0);
        }
        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    public void halfTurn(String type){
        telemetry.addLine("performing half turn " +  type);
        resetAngle();
        if(type.equals("counterclockwise")){
            long time = System.currentTimeMillis();
            telemetry.addLine("time: " + time);
            while (getAngle() <= 87 & (System.currentTimeMillis()<(time+10000))) {
                power = (0.75 /*used to be .75*/ *0.684/5.063) * (-Math.pow((((getAngle())+6.5)/19.5),2) + 4.5*((getAngle()+6.5)/19.5)) + 0.159;
                telemetry.addLine("power: " + power);
                telemetry.update();
                telemetry.addLine("angle: " + getAngle());
                telemetry.update();
                robot.frontLeft.setPower(power);
                robot.backRight.setPower(-power);
                robot.frontRight.setPower(-power);
                robot.backLeft.setPower(power);
            }
            robot.frontLeft.setPower(0);
            robot.backRight.setPower(0);
            robot.frontRight.setPower(0);
            robot.backLeft.setPower(0);
        }
        if(type.equals("clockwise")){
            long time = System.currentTimeMillis();
            while (getAngle() >= -82 && (System.currentTimeMillis()<(time+2000))) {
                power = (.75*0.684/5.063) * (-Math.pow((((-getAngle())+6.5)/19.5),2) + 4.5*((-getAngle()+6.5)/19.5)) + 0.159;
                teleUpdate(""+power,"");
                robot.frontLeft.setPower(-power);
                robot.backRight.setPower(power);
                robot.frontRight.setPower(power);
                robot.backLeft.setPower(-power);
            }
            robot.frontLeft.setPower(0);
            robot.backRight.setPower(0);
            robot.frontRight.setPower(0);
            robot.backLeft.setPower(0);
        }
        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }


    public void encoderDrive(double inches, double pow, String driveMode) throws InterruptedException {
        if(driveMode.equals("drive")){
            //settargetposition is inverse
            //if setpower command for backward is -, then getpowers for both are both positive
            pidDrive.setSetpoint(0);
            pidDrive.setOutputRange(0, power);
            pidDrive.setInputRange(-90, 90);
            pidDrive.enable();
            resetAngle();
            int startPos1 = robot.frontLeft.getCurrentPosition();
            int startPos2 = robot.backLeft.getCurrentPosition();
            int startPos3 = robot.frontRight.getCurrentPosition();
            int startPos4 = robot.backRight.getCurrentPosition();
            robot.frontLeft.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            robot.backRight.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            robot.frontRight.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            robot.backLeft.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            double currentPosInches;
            if(inches<=0) {
                robot.frontLeft.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.backRight.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.frontRight.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.backLeft.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                //robot.changeSpeed(power);
                while (robot.frontRight.getTargetPosition()>robot.frontRight.getCurrentPosition()||
                        robot.frontLeft.getTargetPosition()>robot.frontLeft.getCurrentPosition()||
                        robot.backRight.getTargetPosition()>robot.backRight.getCurrentPosition()||
                        robot.backLeft.getTargetPosition()>robot.backLeft.getCurrentPosition()) {
                    correction = pidDrive.performPID(getAngle());
                    currentPosInches = ((robot.frontLeft.getCurrentPosition() - startPos1) / COUNTS_PER_INCH);
                    teleUpdate("CURRENTPOSINCHES: "+currentPosInches+"","");
                    power = function.getPowerAt(currentPosInches, -inches, pow, "drive");
                    robot.frontLeft.setPower(power + correction);
                    robot.backRight.setPower(power - correction);
                    robot.frontRight.setPower(power - correction);
                    robot.backLeft.setPower(power + correction);
                }
            }
            else{
                //robot.changeSpeed(-power);
                robot.frontLeft.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.backRight.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.frontRight.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                robot.backLeft.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
                while (robot.frontRight.getTargetPosition()<robot.frontRight.getCurrentPosition()||
                        robot.frontLeft.getTargetPosition()<robot.frontLeft.getCurrentPosition()||
                        robot.backRight.getTargetPosition()<robot.backRight.getCurrentPosition()||
                        robot.backLeft.getTargetPosition()<robot.backLeft.getCurrentPosition()) {
                    correction = pidDrive.performPID(getAngle());
                    currentPosInches = ((robot.frontLeft.getCurrentPosition() - startPos1) / COUNTS_PER_INCH * -1);
                    power = -function.getPowerAt(currentPosInches, inches, pow, "drive");
                    teleUpdate("POWER: "+ power+"","");
                    robot.frontLeft.setPower((power + correction));
                    robot.backRight.setPower((power - correction));
                    robot.frontRight.setPower((power - correction));
                    robot.backLeft.setPower((power + correction));
                }
            }
            robot.changeSpeed(0);
        }
        else if(driveMode.equals("strafe")){/////LEFT IS POSITIVE
            pidDrive.setSetpoint(0);
            pidDrive.setOutputRange(0, power);
            pidDrive.setInputRange(-90, 90);
            pidDrive.enable();
            resetAngle();

            int startPos1 = robot.frontLeft.getCurrentPosition();
            int startPos2 = robot.backLeft.getCurrentPosition();
            int startPos3 = robot.frontRight.getCurrentPosition();
            int startPos4 = robot.backRight.getCurrentPosition();
            robot.frontLeft.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            robot.backRight.setTargetPosition((int)(inches*COUNTS_PER_INCH));
            robot.frontRight.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
            robot.backLeft.setTargetPosition((int)(-inches*COUNTS_PER_INCH));
//            telemetry.addLine(robot.frontLeft.getTargetPosition()+" <- TARGET");
//            telemetry.addLine(robot.frontLeft.getCurrentPosition()+" <- Current");
//            telemetry.update();
//            Thread.sleep(2000);
            double currentPosInches;
            //power = 0.9;
            robot.changeSpeed(power);
            if(inches>0) {
                while (robot.frontRight.getTargetPosition()<robot.frontRight.getCurrentPosition()||
                        robot.frontLeft.getTargetPosition()>robot.frontLeft.getCurrentPosition()||
                        robot.backRight.getTargetPosition()>robot.backRight.getCurrentPosition()||
                        robot.backLeft.getTargetPosition()<robot.backLeft.getCurrentPosition()) {
                    telemetry.addData("Correction", correction);
                    telemetry.addLine(robot.frontLeft.getCurrentPosition()+" <- Current");
                    telemetry.update();
                    correction = pidDrive.performPID(getAngle());
                    currentPosInches = ((robot.frontRight.getCurrentPosition() - startPos1) / COUNTS_PER_INCH);
                    power = function.getPowerAt(currentPosInches, inches, pow, "strafe")*1.1;
                    robot.frontLeft.setPower((power + correction));
                    robot.backRight.setPower((power - correction));
                    robot.frontRight.setPower(-(power + correction));
                    robot.backLeft.setPower(-(power - correction));             //STRAFE
                }
            }
            else{
                while (robot.frontRight.getTargetPosition()>robot.frontRight.getCurrentPosition()||
                        robot.frontLeft.getTargetPosition()<robot.frontLeft.getCurrentPosition()||
                        robot.backRight.getTargetPosition()<robot.backRight.getCurrentPosition()||
                        robot.backLeft.getTargetPosition()>robot.backLeft.getCurrentPosition()) {
                    telemetry.addData("Correction", correction);
                    telemetry.update();
                    correction = pidDrive.performPID(getAngle());
                    currentPosInches = ((robot.frontRight.getCurrentPosition() - startPos1) / COUNTS_PER_INCH * -1);
                    power = -function.getPowerAt(currentPosInches, -inches, pow, "strafe")*1.1;
                    robot.frontLeft.setPower((power + correction));
                    robot.backRight.setPower((power - correction));
                    robot.frontRight.setPower(-(power + correction));
                    robot.backLeft.setPower(-(power - correction));             //STRAFE
                }
            }
            robot.changeSpeed(0);
        }
        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
        Thread.sleep(100);
    }


    public void encoderWobble(double inches, double pow) throws InterruptedException {
        robot.wobbleMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.wobbleMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.wobbleMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //settargetposition is inverse
        //if setpower command for backward is -, then getpowers for both are both positive

        int startPos1 = robot.wobbleMotor.getCurrentPosition();
        robot.wobbleMotor.setTargetPosition((int)(inches*COUNTS_PER_INCH));
        telemetry.addLine(Integer.toString(robot.wobbleMotor.getTargetPosition()) + "<-target       current->" + Integer.toString(robot.wobbleMotor.getCurrentPosition()));
        telemetry.update();
//            Thread.sleep(3000);
        if(inches>0) {
            while (robot.wobbleMotor.getTargetPosition() > robot.wobbleMotor.getCurrentPosition()) {
                robot.wobbleMotor.setPower(pow);
                telemetry.addLine(Integer.toString(robot.wobbleMotor.getTargetPosition()) + "<-target       current->" + Integer.toString(robot.wobbleMotor.getCurrentPosition()));
                telemetry.update();
            }
            robot.wobbleMotor.setPower(0);
        }
        else{
            while (robot.wobbleMotor.getTargetPosition() < robot.wobbleMotor.getCurrentPosition()) {
                robot.wobbleMotor.setPower(-pow);
                telemetry.addLine(Integer.toString(robot.wobbleMotor.getTargetPosition()) + "<-target       current->" + Integer.toString(robot.wobbleMotor.getCurrentPosition()));
                telemetry.update();
            }
            robot.wobbleMotor.setPower(0);
        }

        robot.changeMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.changeMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        Thread.sleep(100);
    }


    public void teleUpdate(String label, String description){
        if (robot != null && robot.frontLeft != null && robot.frontRight != null) {
            telemetry.addLine().addData("Current Position",  "Running at %7d :%7d", robot.frontLeft.getCurrentPosition(), robot.frontRight.getCurrentPosition());
        }
        telemetry.addLine().addData(label + ": ", description);
        telemetry.update();
    }


    private double getAngle()
    {
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }
    private void resetAngle()
    {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }
    enum Stage
    {//color difference. greyscale
        detection,//includes outlines
        THRESHOLD,//b&w
        RAW_IMAGE,//displays raw view
    }

    private Stage stageToRenderToViewport = Stage.detection;
    private Stage[] stages = Stage.values();


}
