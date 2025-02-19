/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.engine.execution.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvLink;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseExecutionSysVer;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTag;
import org.cerberus.crud.service.ICountryEnvLinkService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseExecutionSysVerService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.execution.IConditionService;
import org.cerberus.engine.execution.IExecutionRunService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.execution.IRetriesService;
import org.cerberus.engine.execution.IRobotServerService;
import org.cerberus.engine.execution.video.VideoRecorder;
import org.cerberus.engine.gwt.IActionService;
import org.cerberus.engine.gwt.IControlService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.enums.Screenshot;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.robotproviders.IBrowserstackService;
import org.cerberus.service.robotproviders.IKobitonService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.websocket.TestCaseExecutionEndPoint;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@EnableAsync
@Service
public class ExecutionRunService implements IExecutionRunService {

    private static final Logger LOG = LogManager.getLogger(ExecutionRunService.class);

    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IRobotServerService robotServerService;
    @Autowired
    private IActionService actionService;
    @Autowired
    private IControlService controlService;
    @Autowired
    private IConditionService conditionService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseStepExecutionService testCaseStepExecutionService;
    @Autowired
    private ITestCaseStepActionExecutionService testCaseStepActionExecutionService;
    @Autowired
    private ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ITestCaseExecutionSysVerService testCaseExecutionSysVerService;
    @Autowired
    private ICountryEnvLinkService countryEnvLinkService;
    @Autowired
    private ITestCaseExecutionQueueService executionQueueService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private ILoadTestCaseService loadTestCaseService;
    @Autowired
    private IFactoryTestCaseStepExecution factoryTestCaseStepExecution;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;
    @Autowired
    private IFactoryTestCaseStepActionControlExecution factoryTestCaseStepActionControlExecution;
    @Autowired
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;
    @Autowired
    private ExecutionUUID executionUUID;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IRetriesService retriesService;
    @Autowired
    private IFactoryRobotCapability robotCapabilityFactory;
    @Autowired
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private IBrowserstackService browserstackService;
    @Autowired
    private IKobitonService kobitonService;

    @Override
    public TestCaseExecution executeTestCase(TestCaseExecution tCExecution) throws CerberusException {
        long runID = tCExecution.getId();
        String logPrefix = runID + " - ";

        VideoRecorder videoRecorder = null;
        /**
         * Feeding Build Rev of main Application system to
         * testcaseexecutionsysver table. Only if execution is not manual.
         */
        try {

            AnswerItem<String> answerDecode = new AnswerItem<>();

            if (!(tCExecution.isManualURL())) {
                /**
                 * Insert SystemVersion in Database
                 */
                TestCaseExecutionSysVer myExeSysVer = null;
                try {
                    LOG.debug(logPrefix + "Registering Main System Version.");
                    myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, tCExecution.getApplicationObj().getSystem(), tCExecution.getBuild(), tCExecution.getRevision());
                    testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                } catch (CerberusException ex) {
                    LOG.error(logPrefix + ex.getMessage(), ex);
                }
                LOG.debug(logPrefix + "Main System Version Registered.");

                /**
                 * For all Linked environment, we also keep track on the
                 * build/rev information inside testcaseexecutionsysver table.
                 */
                LOG.debug(logPrefix + "Registering Linked System Version.");
                try {
                    List<CountryEnvLink> ceLink = null;
                    ceLink = countryEnvLinkService.convert(countryEnvLinkService.readByVarious(tCExecution.getApplicationObj().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment()));
                    for (CountryEnvLink myCeLink : ceLink) {
                        LOG.debug(logPrefix + "Linked environment found : " + myCeLink.getSystemLink() + myCeLink.getCountryLink() + myCeLink.getEnvironmentLink());

                        CountryEnvParam mycountEnvParam;
                        try {
                            mycountEnvParam = this.countryEnvParamService.convert(this.countryEnvParamService.readByKey(myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink()));
                            myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, myCeLink.getSystemLink(), mycountEnvParam.getBuild(), mycountEnvParam.getRevision());
                            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                        } catch (CerberusException ex) {
                            // Referencial Integrity link between countryEnvLink and CountryEnvParam table should secure that exception to never happen.
                            LOG.error(logPrefix + ex.getMessage(), ex);
                            throw new CerberusException(ex.getMessageError());
                        }
                    }
                } catch (CerberusException ex) {
                    LOG.debug(logPrefix + "No Linked environment found.");
                }
                LOG.debug(logPrefix + "Linked System Version Registered.");
            }

            /**
             * Start robot server if execution is not manual
             */
            if (!tCExecution.getManualExecution().equals("Y")) {
                if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                        || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                        || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                        || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                    // Decoding Robot capabilities.
                    if (tCExecution.getRobotObj() != null) {
                        List<RobotCapability> caps = tCExecution.getRobotObj().getCapabilities();
                        List<RobotCapability> capsDecoded = new ArrayList<>();

                        // TODO ce n'est pas ça encore, faut faire ça au moment ou il recupère l'ip / port
                        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                            int portNumber = 8200;
                            portNumber++;
                        }

                        for (RobotCapability cap : caps) {

                            String capDecoded = "";
                            try {
                                answerDecode = variableService.decodeStringCompletly(cap.getCapability(), tCExecution, null, false);
                                capDecoded = (String) answerDecode.getItem();

                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    LOG.debug(logPrefix + "TestCase interupted due to decode 'Robot Capability key' Error.");
                                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CAPABILITYDECODE)
                                            .resolveDescription("MES", answerDecode.getMessageDescription())
                                            .resolveDescription("FIELD", "")
                                            .resolveDescription("AREA", "Robot Capability key : " + cap.getCapability()));
                                }
                            } catch (CerberusEventException cex) {
                                LOG.warn(cex);
                            }

                            String valDecoded = "";
                            try {
                                answerDecode = variableService.decodeStringCompletly(cap.getValue(), tCExecution, null, false);
                                valDecoded = (String) answerDecode.getItem();

                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    LOG.debug(logPrefix + "TestCase interupted due to decode 'Robot Capability value' Error.");
                                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CAPABILITYDECODE)
                                            .resolveDescription("MES", answerDecode.getMessageDescription())
                                            .resolveDescription("FIELD", "")
                                            .resolveDescription("AREA", "Robot Capability value : " + cap.getValue()));
                                }
                            } catch (CerberusEventException cex) {
                                LOG.warn(cex);
                            }

                            capsDecoded.add(robotCapabilityFactory.create(cap.getId(), cap.getRobot(), capDecoded, valDecoded));
                        }
                        tCExecution.getRobotObj().setCapabilitiesDecoded(capsDecoded);
                    }

                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_STARTINGROBOTSERVER);
                    mes.setDescription(mes.getDescription().replace("%IP%", tCExecution.getRobotHost()));
                    tCExecution.setResultMessage(mes);

                    updateTCExecution(tCExecution, false);

                    if (tCExecution.getRobotHost().equalsIgnoreCase("")) {
                        mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP);
                        mes.setDescription(mes.getDescription().replace("%IP%", tCExecution.getRobotHost()));
                        LOG.debug(logPrefix + mes.getDescription());
                        throw new CerberusException(mes);

                    } else {
                        /**
                         * Start Robot server (Selenium/Appium/Sikuli)
                         */
                        LOG.debug(logPrefix + "Starting Robot Server.");
                        try {
                            this.robotServerService.startServer(tCExecution);
                            LOG.debug(logPrefix + "Robot Server Started.");
                        } catch (CerberusException ex) {
                            LOG.debug(logPrefix + ex.getMessageError().getDescription());
                            throw new CerberusException(ex.getMessageError(), ex);
                        }

                        // Start video
                        try {
                            if (Screenshot.recordVideo(tCExecution.getScreenshot())) {
                                videoRecorder = VideoRecorder.getInstance(tCExecution, recorderService);
                                videoRecorder.beginRecordVideo();
                            }
                        } catch (UnsupportedOperationException ex) {
                            LOG.info(ex.getMessage()); // log only message that application type is not supported
                        }
                    }

                }
            }

            /**
             * For BrowserStack only, we try to enrish the Tag with build hash.
             */
            if (TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK.equals(tCExecution.getRobotProvider())) {
                String newBuildHash = tagService.enrichTagWithBrowserStackBuild(tCExecution.getSystem(), tCExecution.getTag(), tCExecution.getRobotExecutorObj().getHostUser(), tCExecution.getRobotExecutorObj().getHostPassword());
                Tag newTag = tagService.convert(tagService.readByKey(tCExecution.getTag()));
                tCExecution.setTagObj(newTag);
            }

            /**
             * Get used SeleniumCapabilities (empty if application is not GUI)
             */
            LOG.debug(logPrefix + "Getting Selenium capabitities for GUI applications.");
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                try {
                    Capabilities caps = this.robotServerService.getUsedCapabilities(tCExecution.getSession());
                    tCExecution.setVersion(caps.getVersion());
                    tCExecution.setPlatform(caps.getPlatform().toString());
                } catch (Exception ex) {
                    LOG.error(logPrefix + "Exception on selenium getting Used Capabilities :" + ex.toString(), ex);
                }
                LOG.debug(logPrefix + "Selenium capabitities loaded.");
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK) || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                //do nothing, and keep the robot name
            } else {
                // If Selenium is not needed, the selenium and browser info is set to empty.
                tCExecution.setSeleniumIP("");
                tCExecution.setSeleniumPort("");
                tCExecution.setBrowser("");
                tCExecution.setVersion("");
                tCExecution.setPlatform("");
                tCExecution.setRobotDecli("");
                LOG.debug(logPrefix + "No Selenium capabitities loaded because application not (GUI,IPA,APK) : " + tCExecution.getApplicationObj().getType());
            }
            tCExecution.setRobotDecli(tCExecution.getRobotDecli().replace("%BROWSER%", tCExecution.getBrowser()));
            tCExecution.setRobotDecli(tCExecution.getRobotDecli().replace("%BROWSERVERSION%", tCExecution.getVersion()));
            tCExecution.setRobotDecli(tCExecution.getRobotDecli().replace("%PLATFORM%", tCExecution.getPlatform()));

            /**
             * Load Pre TestCase information
             */
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
            LOG.debug(logPrefix + "Loading Pre-testcases.");
            List<TestCase> preTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_PRETESTING, tCExecution.getTestCaseObj().getApplication(), tCExecution.getCountry(),
                    tCExecution.getSystem(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getCountryEnvParam().getRevision());
            if (!(preTests == null)) {
                LOG.debug(logPrefix + "Loaded PreTest List. " + preTests.size() + " found.");
            }
            LOG.debug(logPrefix + "Pre-testcases Loaded.");

            /**
             * Load Pre TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information (Actions & Controls) of all Pre-testcase.");
            List<TestCaseStep> preTestCaseStepList = new ArrayList<>();
            for (TestCase myTCase : preTests) {
                preTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                LOG.debug(logPrefix + "Pre testcase : " + myTCase.getTest() + "-" + myTCase.getTestCase() + " Loaded With all Step(s) found.");
            }
            LOG.debug(logPrefix + "All Steps information (Actions & Controls) of all Pre-testcase Loaded.");

            /**
             * Load Post TestCase information
             */
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
            LOG.debug(logPrefix + "Loading Post-testcases.");
            List<TestCase> postTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_POSTTESTING, tCExecution.getTestCaseObj().getApplication(), tCExecution.getCountry(),
                    tCExecution.getSystem(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getCountryEnvParam().getRevision());
            if (!(postTests == null)) {
                LOG.debug(logPrefix + "Loaded PostTest List. " + postTests.size() + " found.");
            }
            LOG.debug(logPrefix + "Post-testcases Loaded.");

            /**
             * Load Post TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information (Actions & Controls) of all Post-testcase.");
            List<TestCaseStep> postTestCaseStepList = new ArrayList<>();
            for (TestCase myTCase : postTests) {
                postTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                LOG.debug(logPrefix + "Post testcase : " + myTCase.getTest() + "-" + myTCase.getTestCase() + " Loaded With all Step(s) found.");
            }
            LOG.debug(logPrefix + "All Steps information (Actions & Controls) of all Post-testcase Loaded.");

            /**
             * Load Main TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information of Main testcase.");
            List<TestCaseStep> testCaseStepList;
            testCaseStepList = this.loadTestCaseService.loadTestCaseStep(tCExecution.getTestCaseObj());
            tCExecution.getTestCaseObj().setTestCaseStep(testCaseStepList);
            LOG.debug(logPrefix + "Steps information of Main testcase Loaded : " + tCExecution.getTestCaseObj().getTestCaseStep().size() + " Step(s) found.");

            /**
             * Load All properties of the testcase
             */
            LOG.debug(logPrefix + "Loading all Properties.");
            List<TestCaseCountryProperties> tcProperties = new ArrayList<>();
            try {
                tcProperties = testCaseCountryPropertiesService.findAllWithDependencies(tCExecution.getTest(), tCExecution.getTestCase(), tCExecution.getCountry(),
                        tCExecution.getSystem(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getCountryEnvParam().getRevision());
                tCExecution.setTestCaseCountryPropertyList(tcProperties);
            } catch (CerberusException ex) {
                LOG.warn("Exception getting all the properties : ", ex);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(logPrefix + "All Properties Loaded. " + tcProperties.size() + " property(ies) found : " + tcProperties);
            }

            /**
             * Load All Execution Data of testcases that this execution depends
             */
            LOG.debug(logPrefix + "Loading all Execution Data of the execution from queue dependencies.");
            this.testCaseExecutionDataService.loadTestCaseExecutionDataFromDependencies(tCExecution);

            /**
             * Start Execution of the steps/Actions/controls Iterate Steps.
             * mainExecutionTestCaseStepList will contain the list of steps to
             * execute for both pretest and test. This is where we schedule the
             * execution of the steps using mainExecutionTestCaseStepList
             * object.
             */
            LOG.debug(logPrefix + "Starting the execution with step iteration.");
            List<TestCaseStep> mainExecutionTestCaseStepList;
            mainExecutionTestCaseStepList = new ArrayList<>();
            mainExecutionTestCaseStepList.addAll(preTestCaseStepList);
            mainExecutionTestCaseStepList.addAll(testCaseStepList);
            mainExecutionTestCaseStepList.addAll(postTestCaseStepList);

            /**
             * Initialize the global TestCaseExecution Data List.
             */
            //
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING));
            updateTCExecution(tCExecution, true);

            // Evaluate the condition at the step level.
            AnswerItem<Boolean> conditionAnswerTc;
            boolean conditionDecodeError = false;
            /**
             * If execution is not manual, evaluate the condition at the step
             * level
             */
            if (!tCExecution.getManualExecution().equals("Y")) {
                try {
                    answerDecode = variableService.decodeStringCompletly(tCExecution.getConditionVal1(), tCExecution, null, false);
                    tCExecution.setConditionVal1((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITIONDECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value1 "));
                        tCExecution.setEnd(new Date().getTime());
                        LOG.debug(logPrefix + "TestCase interupted due to decode 'TestCase Condition Value1' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(tCExecution.getConditionVal2(), tCExecution, null, false);
                    tCExecution.setConditionVal2((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITIONDECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value2 "));
                        tCExecution.setEnd(new Date().getTime());
                        LOG.debug(logPrefix + "TestCase interupted due to decode 'TestCase Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!conditionDecodeError) {

                conditionAnswerTc = this.conditionService.evaluateCondition(tCExecution.getConditionOper(), tCExecution.getConditionVal1(), tCExecution.getConditionVal2(), tCExecution);
                boolean execute_TestCase = (boolean) conditionAnswerTc.getItem();

                if (execute_TestCase || tCExecution.getManualExecution().equals("Y")) {

                    boolean doStepStopExecution = false;
                    for (TestCaseStep testCaseStep : mainExecutionTestCaseStepList) {

                        // exeMod management : We trigger Forced Step no matter if previous step execution asked to stop.
                        if ((!doStepStopExecution) || (testCaseStep.getForceExe().equalsIgnoreCase("Y"))) {

                            // init the index of the step in case we loop.
                            int step_index = 1;
                            boolean execute_Next_Step = false;
                            TestCaseStepExecution testCaseStepExecution;
                            int maxloop = parameterService.getParameterIntegerByKey("cerberus_loopstep_max", tCExecution.getApplicationObj().getSystem(), 20);

                            // Step Loop management.
                            do {

                                /**
                                 * Start Execution of TestCaseStep
                                 */
                                LOG.debug(logPrefix + "Start execution of testcasestep");
                                long startStep = new Date().getTime();

                                /**
                                 * Create and Register TestCaseStepExecution
                                 */
                                MessageEvent stepMess = new MessageEvent(MessageEventEnum.STEP_PENDING)
                                        .resolveDescription("STEP", String.valueOf(testCaseStep.getSort()))
                                        .resolveDescription("STEPINDEX", String.valueOf(step_index));
                                testCaseStepExecution = factoryTestCaseStepExecution.create(
                                        runID, testCaseStep.getTest(), testCaseStep.getTestCase(),
                                        testCaseStep.getStep(), step_index, testCaseStep.getSort(), testCaseStep.getLoop(), testCaseStep.getConditionOper(), testCaseStep.getConditionVal1(), testCaseStep.getConditionVal2(), testCaseStep.getConditionVal3(), testCaseStep.getConditionVal1(), testCaseStep.getConditionVal2(), testCaseStep.getConditionVal3(), null,
                                        startStep, 0, startStep, 0, new BigDecimal("0"), null, stepMess, testCaseStep, tCExecution,
                                        testCaseStep.getUseStep(), testCaseStep.getUseStepTest(), testCaseStep.getUseStepTestCase(), testCaseStep.getUseStepStep(), testCaseStep.getDescription());
                                testCaseStepExecution.setLoop(testCaseStep.getLoop());
                                testCaseStepExecutionService.insertTestCaseStepExecution(testCaseStepExecution);
                                testCaseStepExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

                                /**
                                 * We populate the TestCaseStep inside the
                                 * execution List
                                 */
                                tCExecution.addTestCaseStepExecutionList(testCaseStepExecution);

                                // determine if step is executed (execute_Step) and if we trigger a new step execution after (execute_Next_Step)
                                boolean execute_Step = true;
                                boolean conditionStepDecodeError = false;
                                boolean conditionStepError = false;
                                AnswerItem<Boolean> conditionAnswer = new AnswerItem<>(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION));
                                if (testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_ONCEIFCONDITIONFALSE)
                                        || testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_ONCEIFCONDITIONTRUE)
                                        || testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_WHILECONDITIONFALSEDO)
                                        || testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_WHILECONDITIONTRUEDO)
                                        || testCaseStepExecution.getLoop().equals("")
                                        || step_index > 1) {
                                    // Decode Conditionvalue1 and Conditionvalue2 and Evaluate the condition at the Step level.
                                    try {
                                        answerDecode = variableService.decodeStringCompletly(testCaseStepExecution.getConditionVal1(), tCExecution, null, false);
                                        testCaseStepExecution.setConditionVal1((String) answerDecode.getItem());
                                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                                            testCaseStepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                            testCaseStepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value1"));
                                            testCaseStepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value1").getDescription());
                                            testCaseStepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                            testCaseStepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                            testCaseStepExecution.setEnd(new Date().getTime());
                                            LOG.debug(logPrefix + "Step interupted due to decode 'Step Condition Value1' Error.");
                                            conditionStepDecodeError = true;
                                        }
                                    } catch (CerberusEventException cex) {
                                        LOG.warn(cex);
                                    }
                                    if (!conditionStepDecodeError) {
                                        try {
                                            answerDecode = variableService.decodeStringCompletly(testCaseStepExecution.getConditionVal2(), tCExecution, null, false);
                                            testCaseStepExecution.setConditionVal2((String) answerDecode.getItem());
                                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                                testCaseStepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                                testCaseStepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value2"));
                                                testCaseStepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value2").getDescription());
                                                testCaseStepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                                testCaseStepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                                testCaseStepExecution.setEnd(new Date().getTime());
                                                LOG.debug(logPrefix + "Step interupted due to decode 'Step Condition Value2' Error.");
                                                conditionStepDecodeError = true;
                                            }
                                        } catch (CerberusEventException cex) {
                                            LOG.warn(cex);
                                        }
                                    }
                                    if (!(conditionStepDecodeError)) {

                                        conditionAnswer = this.conditionService.evaluateCondition(testCaseStepExecution.getConditionOper(), testCaseStepExecution.getConditionVal1(), testCaseStepExecution.getConditionVal2(), tCExecution);
                                        execute_Step = (boolean) conditionAnswer.getItem();
                                        if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")) {
                                            // There were no error when performing the condition evaluation.
                                            switch (testCaseStepExecution.getLoop()) {
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONFALSE:
                                                    execute_Step = !execute_Step;
                                                    execute_Next_Step = false;
                                                    break;
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONTRUE:
                                                case "":
                                                    execute_Next_Step = false;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONFALSEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONFALSE:
                                                    execute_Step = !execute_Step;
                                                    execute_Next_Step = execute_Step;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONTRUEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONTRUE:
                                                    execute_Next_Step = execute_Step;
                                                    break;
                                                default:
                                                    execute_Next_Step = false;
                                            }
                                        } else {
                                            // Error when performing the condition evaluation. We force no execution (false)
                                            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                                            mes.setDescription(mes.getDescription()
                                                    .replace("%AREA%", "step ")
                                                    .replace("%COND%", testCaseStepExecution.getConditionOper())
                                                    .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                                            tCExecution.setResultMessage(mes);
                                            testCaseStepExecution.setExecutionResultMessage(mes);

                                            testCaseStepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_FAILED)
                                                    .resolveDescription("AREA", "")
                                                    .resolveDescription("COND", testCaseStepExecution.getConditionOper())
                                                    .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription())
                                            );

                                            testCaseStepExecution.setEnd(new Date().getTime());
                                            LOG.debug(logPrefix + "Step interupted due to condition error.");
                                            conditionStepError = true;
                                            execute_Next_Step = false;
                                            execute_Step = false;
                                        }
                                    } else {

                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITIONDECODE)
                                                .resolveDescription("AREA", "Step ")
                                                .resolveDescription("MES", answerDecode.getMessageDescription()));
                                        tCExecution.setEnd(new Date().getTime());
                                        LOG.debug(logPrefix + "TestCase interupted due to decode Condition Error.");

                                        // There was an error on decode so we stop everything.
                                        if (tCExecution.getManualExecution().equals("Y")) {
                                            execute_Next_Step = true;
                                            execute_Step = true;
                                        } else {
                                            execute_Next_Step = false;
                                            execute_Step = false;
                                        }
                                    }
                                } else if (testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONFALSE)
                                        || testCaseStepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONTRUE)) {
                                    // First Step execution for LOOP_DOWHILECONDITIONTRUE and LOOP_DOWHILECONDITIONFALSE --> We force the step execution and activate the next step execution.
                                    execute_Step = true;
                                    execute_Next_Step = true;
                                } else {
                                    // First Step execution for Unknown Loop --> We force the step execution only once (default behaviour).
                                    execute_Step = true;
                                    execute_Next_Step = false;
                                    conditionAnswer.setResultMessage(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION));
                                }

                                /**
                                 * Execute Step
                                 */
                                LOG.debug(logPrefix + "Executing step : " + testCaseStepExecution.getTest() + " - " + testCaseStepExecution.getTestCase() + " - Step " + testCaseStepExecution.getStep() + " - Index " + testCaseStepExecution.getStep());

                                if (execute_Step) {

                                    /**
                                     * We execute the step
                                     */
                                    testCaseStepExecution = this.executeStep(testCaseStepExecution, tCExecution);

                                    /**
                                     * Updating Execution Result Message only if
                                     * execution result message of the step is
                                     * not PE or OK.
                                     */
                                    if ((!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED))))
                                            && (!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))) {
                                        tCExecution.setResultMessage(testCaseStepExecution.getExecutionResultMessage());
                                    }
                                    if (testCaseStepExecution.getStepResultMessage().equals(new MessageEvent(MessageEventEnum.STEP_PENDING))) {
                                        testCaseStepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.STEP_SUCCESS));
                                    }

                                    testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

                                    if (testCaseStepExecution.isStopExecution()) {
                                        break;
                                    }

                                } else // We don't execute the step and record a generic execution.
                                if ((!conditionStepDecodeError) && (!conditionStepError)) {

                                    /**
                                     * Register Step in database
                                     */
                                    LOG.debug(logPrefix + "Registering Step : " + testCaseStepExecution.getStep());

                                    // We change the Step message only if the Step is not executed due to condition.
                                    MessageEvent stepMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_NOTEXECUTED);
                                    testCaseStepExecution.setStepResultMessage(stepMes);
                                    testCaseStepExecution.setReturnMessage(testCaseStepExecution.getReturnMessage()
                                            .replace("%COND%", testCaseStepExecution.getConditionOper())
                                            .replace("%LOOP%", testCaseStepExecution.getLoop())
                                            .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                                    );

                                    testCaseStepExecution.setEnd(new Date().getTime());
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);
                                    LOG.debug(logPrefix + "Registered Step");

                                } else {
                                    // Not executed because decode error or failed condition.
                                    testCaseStepExecution.setEnd(new Date().getTime());
                                    testCaseStepExecution.setStopExecution(true);
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);
                                    LOG.debug(logPrefix + "Registered Step");
                                }

                                /**
                                 * Log TestCaseStepExecution
                                 */
                                if (tCExecution.getVerbose() > 0) {
                                    LOG.info(testCaseStepExecution.toJson(false, true));
                                }

                                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                                updateTCExecutionWebSocketOnly(tCExecution, false);

                                step_index++;
                            } while (execute_Next_Step && step_index <= maxloop);

                            // Step execution boolean is considered for next step execution only if current step was not forced or forced and failed.
                            if (!testCaseStep.getForceExe().equalsIgnoreCase("Y") || testCaseStepExecution.isStopExecution()) {
                                doStepStopExecution = testCaseStepExecution.isStopExecution();
                            }
                        }
                    }

                    /**
                     * If at that time the execution is still PE, we move it to
                     * OK. It means that no issue were met.
                     */
                    if ((tCExecution.getResultMessage() == null) || (tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED)))) {
                        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK));
                    }

                    /**
                     * We notify external robot provider of end of execution
                     * status.
                     */
                    switch (tCExecution.getRobotProvider()) {
                        case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                            browserstackService.setSessionStatus(tCExecution.getSystem(), tCExecution.getRobotSessionID(), tCExecution.getControlStatus(), tCExecution.getControlMessage(), tCExecution.getRobotExecutorObj().getHostUser(), tCExecution.getRobotExecutorObj().getHostPassword());
                            break;
                        case TestCaseExecution.ROBOTPROVIDER_KOBITON:
                            kobitonService.setSessionStatus(tCExecution.getSystem(), tCExecution.getRobotSessionID(), tCExecution.getControlStatus(), tCExecution.getControlMessage(), tCExecution.getRobotExecutorObj().getHostUser(), tCExecution.getRobotExecutorObj().getHostPassword());
                            break;
                    }

                } else { // We don't execute the testcase linked with condition.
                    MessageGeneral mes;
                    /**
                     * Update Execution status from condition
                     */
                    if (conditionAnswerTc.getResultMessage().getMessage().getCodeString().equals("PE")) {
                        mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_NE_CONDITION);
                    } else {
                        mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    }
                    mes.setDescription(mes.getDescription().replace("%COND%", tCExecution.getConditionOper())
                            .replace("%MES%", conditionAnswerTc.getResultMessage().getDescription()));
                    tCExecution.setResultMessage(mes);
                }
            }
        } catch (CerberusException ex) {
            /**
             * If an exception is found, set the execution to FA and print the
             * exception
             */
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessageError().getDescription());
            tCExecution.setResultMessage(messageFin);
            tCExecution.setControlMessage(tCExecution.getControlMessage() + " Exception: " + ex);
            LOG.error(logPrefix + "Exception found Executing Test " + tCExecution.getId() + " Exception : " + ex.toString());
        } catch (Exception ex) {
            /**
             * If an exception is found, set the execution to FA and print the
             * exception
             */
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessage());
            tCExecution.setResultMessage(messageFin);
            tCExecution.setControlMessage(tCExecution.getControlMessage() + " Exception: " + ex);
            LOG.error(logPrefix + "Exception found Executing Test " + tCExecution.getId(), ex);
        } finally {

            /**
             * We stop the server session here (selenium for ex.).
             */
            try {
                if (videoRecorder != null) {
                    videoRecorder.endRecordVideo();
                }
                tCExecution = this.stopTestCase(tCExecution);
            } catch (Exception ex) {
                LOG.error(logPrefix + "Exception Stopping Test " + tCExecution.getId() + " Exception : " + ex.toString(), ex);
            }

            /**
             * Log Execution
             */
            LOG.info(tCExecution.toJson(false));

            /**
             * Clean memory
             */
            try {
                executionUUID.removeExecutionUUID(tCExecution.getExecutionUUID());
                LOG.debug(logPrefix + "Clean ExecutionUUID");
            } catch (Exception ex) {
                LOG.error(logPrefix + "Exception cleaning Memory: " + ex.toString(), ex);
            }

            /**
             * Log execution is finished
             */
            LOG.info("Execution Finished : UUID=" + tCExecution.getExecutionUUID()
                    + "__ID=" + tCExecution.getId() + "__RC=" + tCExecution.getControlStatus() + "__"
                    + "TestName=" + tCExecution.getEnvironment() + "." + tCExecution.getCountry() + "."
                    + tCExecution.getBuild() + "." + tCExecution.getRevision() + "." + tCExecution.getTest() + "_"
                    + tCExecution.getTestCase() + "_" + tCExecution.getTestCaseObj().getDescription().replace(".", ""));

            /**
             * Retry management, in case the result is not (OK or NE), we
             * execute the job again reducing the retry to 1.
             */
            boolean isRetried = retriesService.manageRetries(tCExecution);

            /**
             * Updating queue to done status only for execution from queue
             */
            if (tCExecution.getQueueID() != 0) {
                executionQueueService.updateToDone(tCExecution.getQueueID(), "", runID);
            }

            /**
             * After every execution finished, <br>
             * if the execution has a tag that has a campaign associated  <br>
             * and no more executions are in the queue, <br>
             * we trigger : <br>
             * 1/ The update of the EndExeQueue of the tag <br>
             * 2/ We notify the Distribution List with execution report status
             */
            tagService.manageCampaignEndOfExecution(tCExecution.getTag());

            /**
             * Dependency management, At the end of the execution, we RELEASE
             * the corresponding dependencies and put corresponding Queue
             * entries to QUEUED status.
             */
            if (!isRetried) {
                testCaseExecutionQueueDepService.manageDependenciesEndOfExecution(tCExecution);
            }

            // After every execution finished we try to trigger more from the queue;-).
            executionThreadPoolService.executeNextInQueueAsynchroneously(false);

        }

        return tCExecution;

    }

    // Update Execution status and eventually push the new value to websocket.
    private void updateTCExecution(TestCaseExecution tCExecution, boolean forcePush) {
        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }

        updateTCExecutionWebSocketOnly(tCExecution, forcePush);

    }

    private void updateTCExecutionWebSocketOnly(TestCaseExecution tCExecution, boolean forcePush) {
        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        if (tCExecution.isCerberus_featureflipping_activatewebsocketpush()) {
            TestCaseExecutionEndPoint.getInstance().send(tCExecution, forcePush);
        }

    }

    @Override
    public TestCaseExecution stopTestCase(TestCaseExecution tCExecution) {

        /**
         * Stop Execution
         */
        LOG.debug(tCExecution.getId() + " - Stop the execution " + tCExecution.getId() + " UUID:" + tCExecution.getExecutionUUID());
        try {
            //TODO:FN debug messages to be removed
            LOG.debug("[DEBUG] STOP " + "__ID=" + tCExecution.getId());
            this.stopRunTestCase(tCExecution);
        } catch (Exception ex) {
            LOG.warn("Exception Stopping Execution " + tCExecution.getId() + " Exception :" + ex.toString());
        }

        /**
         * Collecting and calculating Statistics.
         */
        try {
//            this.collectExecutionStats(tCExecution);
        } catch (Exception ex) {
            LOG.warn("Exception collecting stats for execution " + tCExecution.getId() + " Exception:" + ex.toString());
        }

        /**
         * Saving TestCaseExecution object.
         */
        tCExecution.setEnd(new Date().getTime());

        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            LOG.warn("Exception updating Execution :" + tCExecution.getId() + " Exception:" + ex.toString());
        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        if (tCExecution.isCerberus_featureflipping_activatewebsocketpush()) {
            TestCaseExecutionEndPoint.getInstance().send(tCExecution, true);
            TestCaseExecutionEndPoint.getInstance().end(tCExecution);
        }

        return tCExecution;
    }

    private TestCaseStepExecution executeStep(TestCaseStepExecution testCaseStepExecution, TestCaseExecution tcExecution) {

        long runID = testCaseStepExecution.getId();
        String logPrefix = runID + " - ";
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // Initialise the Step Data List.
        List<TestCaseExecutionData> myStepDataList = new ArrayList<TestCaseExecutionData>();
        testCaseStepExecution.setTestCaseExecutionDataList(myStepDataList);
        // Initialise the Data List used to enter the action.
        /**
         * Iterate Actions
         */
        List<TestCaseStepAction> testCaseStepActionList = testCaseStepExecution.getTestCaseStep().getTestCaseStepAction();
        LOG.debug("Getting list of actions of the step. " + testCaseStepActionList.size() + " action(s) to perform.");

        for (TestCaseStepAction testCaseStepAction : testCaseStepActionList) {

            /**
             * Start Execution of TestCaseStepAction
             */
            long startAction = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionExecution.
             */
            TestCaseStepActionExecution testCaseStepActionExecution = factoryTestCaseStepActionExecution.create(
                    testCaseStepExecution.getId(), testCaseStepAction.getTest(), testCaseStepAction.getTestCase(),
                    testCaseStepAction.getStep(), testCaseStepExecution.getIndex(), testCaseStepAction.getSequence(), testCaseStepAction.getSort(), null, null,
                    testCaseStepAction.getConditionOper(), testCaseStepAction.getConditionVal1(), testCaseStepAction.getConditionVal2(), testCaseStepAction.getConditionVal3(),
                    testCaseStepAction.getConditionVal1(), testCaseStepAction.getConditionVal2(), testCaseStepAction.getConditionVal3(),
                    testCaseStepAction.getAction(), testCaseStepAction.getValue1(), testCaseStepAction.getValue2(), testCaseStepAction.getValue3(), testCaseStepAction.getValue1(),
                    testCaseStepAction.getValue2(), testCaseStepAction.getValue3(),
                    testCaseStepAction.getForceExeStatus(), startAction, 0, startAction, 0, new MessageEvent(MessageEventEnum.ACTION_PENDING),
                    testCaseStepAction.getDescription(), testCaseStepAction, testCaseStepExecution);
            this.testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);

            /**
             * We populate the TestCase Action List
             */
            testCaseStepExecution.addTestCaseStepActionExecutionList(testCaseStepActionExecution);

            /**
             * If execution is not manual, evaluate the condition at the action
             * level
             */
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!tcExecution.getManualExecution().equals("Y")) {

                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getConditionVal1(), tcExecution, null, false);
                    testCaseStepActionExecution.setConditionVal1((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value1"));
                        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        LOG.debug("Action interupted due to decode 'Action Condition Value1' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }

                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getConditionVal2(), tcExecution, null, false);
                    testCaseStepActionExecution.setConditionVal2((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value2"));
                        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        LOG.debug("Action interupted due to decode 'Action Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {
                conditionAnswer = this.conditionService.evaluateCondition(testCaseStepActionExecution.getConditionOper(), testCaseStepActionExecution.getConditionVal1(), testCaseStepActionExecution.getConditionVal2(), tcExecution);
                boolean execute_Action = (boolean) conditionAnswer.getItem();

                /**
                 * If condition OK or if manual execution, then execute the
                 * action
                 */
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || tcExecution.getManualExecution().equals("Y")) {

                    // Execute or not the action here.
                    if (execute_Action || tcExecution.getManualExecution().equals("Y")) {
                        LOG.debug("Executing action : " + testCaseStepActionExecution.getAction() + " with val1 : " + testCaseStepActionExecution.getValue1()
                                + " and val2 : " + testCaseStepActionExecution.getValue2());

                        /**
                         * We execute the Action
                         */
                        testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution, tcExecution);

                        /**
                         * If Action or property reported to stop the testcase,
                         * we stop it and update the step with the message.
                         */
                        testCaseStepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
                        if ((!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))
                                && (!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING))))) {
                            testCaseStepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
                            testCaseStepExecution.setStepResultMessage(testCaseStepActionExecution.getActionResultMessage());
                        }

                        if (testCaseStepActionExecution.isStopExecution()) {
                            break;
                        }

                    } else { // We don't execute the action and record a generic execution.

                        /**
                         * Record Screenshot, PageSource
                         */
                        testCaseStepActionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(testCaseStepActionExecution, null));

                        LOG.debug("Registering Action : " + testCaseStepActionExecution.getAction());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent actionMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_NOTEXECUTED);
                        testCaseStepActionExecution.setActionResultMessage(actionMes);
                        testCaseStepActionExecution.setReturnMessage(testCaseStepActionExecution.getReturnMessage()
                                .replace("%COND%", testCaseStepActionExecution.getConditionOper())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                        LOG.debug("Registered Action");

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", testCaseStepActionExecution.getConditionOper())
                            .replace("%AREA%", "action ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    testCaseStepActionExecution.setExecutionResultMessage(mes);
                    testCaseStepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());

                    testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", testCaseStepActionExecution.getConditionOper())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    testCaseStepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_FAILED)
                            .resolveDescription("AREA", "action ")
                            .resolveDescription("COND", testCaseStepActionExecution.getConditionOper())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    if (testCaseStepActionExecution.getForceExeStatus().equals("PE")) {
                        testCaseStepActionExecution.setStopExecution(false);
                        MessageEvent actionMes = testCaseStepActionExecution.getActionResultMessage();
                        actionMes.setDescription(testCaseStepActionExecution.getActionResultMessage().getDescription() + " -- Execution forced to continue.");
                        testCaseStepActionExecution.setActionResultMessage(actionMes);
                    } else {
                        testCaseStepActionExecution.setStopExecution(true);
                    }

                    testCaseStepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());

                    testCaseStepActionExecution.setEnd(new Date().getTime());

                    this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                    LOG.debug("Action interupted due to condition error.");
                    // We stop any further Action execution.
                    if (testCaseStepActionExecution.isStopExecution()) {
                        break;
                    }
                }
            } else {

                testCaseStepActionExecution.setEnd(new Date().getTime());
                testCaseStepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
                testCaseStepExecution.setStepResultMessage(testCaseStepActionExecution.getActionResultMessage());
                testCaseStepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
                this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                LOG.debug("Registered Action");
                if (testCaseStepActionExecution.isStopExecution()) {
                    break;
                }

            }

            /**
             * Log TestCaseStepActionExecution
             */
            if (tcExecution.getVerbose() > 0) {
                LOG.info(testCaseStepActionExecution.toJson(false, true));
            }

        }
        testCaseStepExecution.setEnd(new Date().getTime());

        this.testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

        updateTCExecutionWebSocketOnly(tcExecution, false);

        return testCaseStepExecution;
    }

    private TestCaseStepActionExecution executeAction(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseExecution tcExecution) {

        LOG.debug("Starting execute Action : " + testCaseStepActionExecution.getAction());
        AnswerItem<String> answerDecode = new AnswerItem<>();

        /**
         * If execution is not manual, do action and record files
         */
        if (!tcExecution.getManualExecution().equals("Y")) {
            testCaseStepActionExecution = this.actionService.doAction(testCaseStepActionExecution);

            /**
             * Record Screenshot, PageSource
             */
            try {
                testCaseStepActionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(testCaseStepActionExecution, null));
            } catch (Exception ex) {
                LOG.warn("Unable to record Screenshot/PageSource : " + ex.toString(), ex);
            }

        } else {
            /**
             * If execution manual, set Action result message as notExecuted
             */
            testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.ACTION_WAITINGFORMANUALEXECUTION));
            testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            testCaseStepActionExecution.setEnd(new Date().getTime());
        }

        /**
         * Register Action in database
         */
        LOG.debug("Registering Action : " + testCaseStepActionExecution.getAction());
        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
        LOG.debug("Registered Action");

        if (testCaseStepActionExecution.isStopExecution()) {
            return testCaseStepActionExecution;
        }
        //As controls are associated with an action, the current state for the action is stored in order to restore it
        //if some property is not defined for the country
        MessageEvent actionMessage = testCaseStepActionExecution.getActionResultMessage();
        MessageGeneral excutionResultMessage = testCaseStepActionExecution.getExecutionResultMessage();
        /**
         * Iterate Control
         */
        List<TestCaseStepActionControl> tcsacList = testCaseStepActionExecution.getTestCaseStepAction().getTestCaseStepActionControl();
        for (TestCaseStepActionControl testCaseStepActionControl : tcsacList) {

            /**
             * Start Execution of TestCAseStepActionControl
             */
            long startControl = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionControlExecution
             */
            LOG.debug("Creating TestCaseStepActionControlExecution");
            TestCaseStepActionControlExecution testCaseStepActionControlExecution
                    = factoryTestCaseStepActionControlExecution.create(testCaseStepActionExecution.getId(), testCaseStepActionControl.getTest(), testCaseStepActionControl.getTestCase(),
                            testCaseStepActionControl.getStep(), testCaseStepActionExecution.getIndex(), testCaseStepActionControl.getSequence(), testCaseStepActionControl.getControlSequence(), testCaseStepActionControl.getSort(),
                            null, null,
                            testCaseStepActionControl.getConditionOper(), testCaseStepActionControl.getConditionVal1(), testCaseStepActionControl.getConditionVal2(), testCaseStepActionControl.getConditionVal3(), testCaseStepActionControl.getConditionVal1(), testCaseStepActionControl.getConditionVal2(), testCaseStepActionControl.getConditionVal3(),
                            testCaseStepActionControl.getControl(), testCaseStepActionControl.getValue1(), testCaseStepActionControl.getValue2(), testCaseStepActionControl.getValue3(), testCaseStepActionControl.getValue1(), testCaseStepActionControl.getValue2(),
                            testCaseStepActionControl.getValue3(), testCaseStepActionControl.getFatal(), startControl, 0, 0, 0,
                            testCaseStepActionControl.getDescription(), testCaseStepActionExecution, new MessageEvent(MessageEventEnum.CONTROL_PENDING));
            this.testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);

            LOG.debug("Executing control : " + testCaseStepActionControlExecution.getControlSequence() + " type : " + testCaseStepActionControlExecution.getControl());

            /**
             * We populate the TestCase Control List
             */
            testCaseStepActionExecution.addTestCaseStepActionExecutionList(testCaseStepActionControlExecution);

            // Evaluate the condition at the control level.
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!tcExecution.getManualExecution().equals("Y")) {
                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getConditionVal1(), tcExecution, null, false);
                    testCaseStepActionControlExecution.setConditionVal1((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value1"));
                        testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionControlExecution.setEnd(new Date().getTime());
                        LOG.debug("Control interupted due to decode 'Control Condition Value1' Error.");
                        conditionDecodeError = true;
                    }

                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionControlExecution.getConditionVal2(), tcExecution, null, false);
                    testCaseStepActionControlExecution.setConditionVal2((String) answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionControlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value2"));
                        testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionControlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionControlExecution.setEnd(new Date().getTime());
                        LOG.debug("Control interupted due to decode 'Control Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {

                conditionAnswer = this.conditionService.evaluateCondition(testCaseStepActionControlExecution.getConditionOper(), testCaseStepActionControlExecution.getConditionVal1(), testCaseStepActionControlExecution.getConditionVal2(), tcExecution);
                boolean execute_Control = (boolean) conditionAnswer.getItem();
                /**
                 * If condition OK or if manual execution, then execute the
                 * control
                 */
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || tcExecution.getManualExecution().equals("Y")) {

                    if (execute_Control || tcExecution.getManualExecution().equals("Y")) {

                        /**
                         * We execute the control
                         */
                        testCaseStepActionControlExecution = executeControl(testCaseStepActionControlExecution, tcExecution);

                        /**
                         * We update the Action with the execution message and
                         * stop flag from the control. We update the status only
                         * if the control is not OK. This is to prevent moving
                         * the status to OK when it should stay KO when a
                         * control failed previously.
                         */
                        testCaseStepActionExecution.setStopExecution(testCaseStepActionControlExecution.isStopExecution());
                        if (!(testCaseStepActionControlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                            //NA is a special case of not having success while calculating the property; the action shouldn't be stopped
                            if (testCaseStepActionControlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION))) {
                                //restores the messages information if the property is not defined for the country
                                testCaseStepActionExecution.setActionResultMessage(actionMessage);
                                testCaseStepActionExecution.setExecutionResultMessage(excutionResultMessage);
                            } else {
                                testCaseStepActionExecution.setExecutionResultMessage(testCaseStepActionControlExecution.getExecutionResultMessage());
                                testCaseStepActionExecution.setActionResultMessage(testCaseStepActionControlExecution.getControlResultMessage());
                            }
                        }
                        /**
                         * If Control reported to stop the testcase, we stop it.
                         */
                        if (testCaseStepActionControlExecution.isStopExecution()) {
                            break;
                        }

                    } else { // We don't execute the control and record a generic execution.

                        /**
                         * Record Screenshot, PageSource
                         */
                        testCaseStepActionControlExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution));

                        /**
                         * Register Control in database
                         */
                        LOG.debug("Registering Control : " + testCaseStepActionControlExecution.getControlSequence());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent controlMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_NOTEXECUTED);
                        testCaseStepActionControlExecution.setControlResultMessage(controlMes);
                        testCaseStepActionControlExecution.setReturnMessage(testCaseStepActionControlExecution.getReturnMessage()
                                .replace("%COND%", testCaseStepActionControlExecution.getConditionOper())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        testCaseStepActionControlExecution.setEnd(new Date().getTime());
                        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
                        LOG.debug("Registered Control");

                        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                        updateTCExecutionWebSocketOnly(tcExecution, false);

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", testCaseStepActionControlExecution.getConditionOper())
                            .replace("%AREA%", "control ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    testCaseStepActionControlExecution.setExecutionResultMessage(mes);
                    testCaseStepActionExecution.setExecutionResultMessage(mes);

                    testCaseStepActionControlExecution.setControlResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", testCaseStepActionControlExecution.getConditionOper())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "control ")
                            .resolveDescription("COND", testCaseStepActionControlExecution.getConditionOper())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    testCaseStepActionControlExecution.setEnd(new Date().getTime());

                    this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
                    LOG.debug("Control interupted due to condition error.");
                    // We stop any further Control execution.
                    break;
                }
            } else {

                testCaseStepActionControlExecution.setEnd(new Date().getTime());
                testCaseStepActionExecution.setExecutionResultMessage(testCaseStepActionControlExecution.getExecutionResultMessage());
                testCaseStepActionExecution.setActionResultMessage(testCaseStepActionControlExecution.getControlResultMessage());
                this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
                LOG.debug("Registered Control");

                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                updateTCExecutionWebSocketOnly(tcExecution, false);
            }

            /**
             * Log TestCaseStepActionControlExecution
             */
            if (tcExecution.getVerbose() > 0) {
                LOG.info(testCaseStepActionControlExecution.toJson(false, true));
            }

        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateTCExecutionWebSocketOnly(tcExecution, false);

        LOG.debug("Finished execute Action : " + testCaseStepActionExecution.getAction());
        return testCaseStepActionExecution;

    }

    private TestCaseStepActionControlExecution executeControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution, TestCaseExecution tcExecution) {

        /**
         * If execution is not manual, do control and record files
         */
        if (!tcExecution.getManualExecution().equals("Y")) {
            testCaseStepActionControlExecution = this.controlService.doControl(testCaseStepActionControlExecution);

            /**
             * Record Screenshot, PageSource
             */
            testCaseStepActionControlExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution));
        } else {
            /**
             * If execution manual, set Control result message as notExecuted
             */
            testCaseStepActionControlExecution.setControlResultMessage(new MessageEvent(MessageEventEnum.CONTROL_WAITINGEXECUTION));
            testCaseStepActionControlExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            testCaseStepActionControlExecution.setEnd(new Date().getTime());
        }

        /**
         * Register Control in database
         */
        LOG.debug("Registering Control : " + testCaseStepActionControlExecution.getControlSequence());
        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
        LOG.debug("Registered Control");

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateTCExecutionWebSocketOnly(tcExecution, false);

        return testCaseStepActionControlExecution;
    }

    private TestCaseExecution stopRunTestCase(TestCaseExecution tCExecution) {

        switch (tCExecution.getApplicationObj().getType()) {
            case Application.TYPE_GUI:
            case Application.TYPE_APK:
            case Application.TYPE_IPA:
                try {
                    this.robotServerService.stopServer(tCExecution);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Stop server for execution " + tCExecution.getId());
                    }
                } catch (WebDriverException exception) {
                    LOG.warn("Selenium/Appium didn't manage to close connection for execution " + tCExecution.getId(), exception);
                }
                break;
            case Application.TYPE_FAT:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stop Sikuli server for execution " + tCExecution.getId() + " closing application " + tCExecution.getCountryEnvironmentParameters().getIp());
                }
                if (!StringUtil.isNullOrEmpty(tCExecution.getCountryEnvironmentParameters().getIp())) {
                    this.sikuliService.doSikuliActionCloseApp(tCExecution.getSession(), tCExecution.getCountryEnvironmentParameters().getIp());
                }
                break;
            default:
        }

//        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
//                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
//                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
//            try {
//                this.robotServerService.stopServer(tCExecution);
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("Stop server for execution " + tCExecution.getId());
//                }
//            } catch (WebDriverException exception) {
//                LOG.warn("Selenium didn't manage to close connection for execution " + tCExecution.getId() + " due to " + exception.toString(), exception);
//            }
//        }
//        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Stop Sikuli server for execution " + tCExecution.getId() + " closing application " + tCExecution.getCountryEnvironmentParameters().getIp());
//            }
//            if (!StringUtil.isNullOrEmpty(tCExecution.getCountryEnvironmentParameters().getIp())) {
//                this.sikuliService.doSikuliActionCloseApp(tCExecution.getSession(), tCExecution.getCountryEnvironmentParameters().getIp());
//            }
//        }
        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateTCExecutionWebSocketOnly(tCExecution, false);

        return tCExecution;
    }

    @Override
    @Async
    public TestCaseExecution executeTestCaseAsynchroneously(TestCaseExecution tCExecution) throws CerberusException {
        try {
            return executeTestCase(tCExecution);
        } catch (CerberusException ex) {
            throw new CerberusException(ex.getMessageError());
        }
    }

}
