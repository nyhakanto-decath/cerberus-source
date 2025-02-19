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
package org.cerberus.servlet.crud.testexecution;

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.crud.service.impl.LabelService;
import org.cerberus.dto.SummaryStatisticsBugTrackerDTO;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.dto.TreeNode;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadTestCaseExecutionByTag", urlPatterns = {"/ReadTestCaseExecutionByTag"})
public class ReadTestCaseExecutionByTag extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private ITagService tagService;
    private ITestCaseExecutionQueueService testCaseExecutionInQueueService;
    private ITestCaseLabelService testCaseLabelService;
    private ILabelService labelService;
    private IFactoryTestCase factoryTestCase;

    private static final Logger LOG = LogManager.getLogger("ReadTestCaseExecutionByTag");

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        String echo = request.getParameter("sEcho");

        AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        tagService = appContext.getBean(ITagService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);

        try {
            // Data/Filter Parameters.
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            List<String> outputReport = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("outputReport"), new ArrayList<>(), "UTF-8");

            JSONObject jsonResponse = new JSONObject();
            JSONObject statusFilter = getStatusList(request);
            JSONObject countryFilter = getCountryList(request, appContext);

            //Get Data from database
            List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(Tag);

            List<TestCaseLabel> testCaseLabelScopeList = null;
            if (outputReport.isEmpty() || outputReport.contains("labelStat") || outputReport.contains("table")) {
                String testCaseKey = "";
                HashMap<String, TestCase> ttc = new HashMap<>();
                List<TestCase> tcList = new ArrayList<>();
                for (TestCaseExecution testCaseExecution : testCaseExecutions) {
                    testCaseKey = testCaseExecution.getTest() + "__" + testCaseExecution.getTestCase();
                    if (ttc.get(testCaseKey) == null) {
                        ttc.put(testCaseKey, factoryTestCase.create(testCaseExecution.getTest(), testCaseExecution.getTestCase()));
                        tcList.add(factoryTestCase.create(testCaseExecution.getTest(), testCaseExecution.getTestCase()));
                    }

                }

                testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
                AnswerList testCaseLabelList = testCaseLabelService.readByTestTestCase(null, null, tcList);
                testCaseLabelScopeList = testCaseLabelList.getDataList();
            }

            // Table that contain the list of testcases and corresponding executions
            if (outputReport.isEmpty() || outputReport.contains("table")) {
                jsonResponse.put("table", generateTestCaseExecutionTable(appContext, testCaseExecutions, statusFilter, countryFilter, testCaseLabelScopeList));
            }
            // Executions per Function (or Test).
            if (outputReport.isEmpty() || outputReport.contains("functionChart")) {
                jsonResponse.put("functionChart", generateFunctionChart(testCaseExecutions, Tag, statusFilter, countryFilter));
            }
            // Global executions stats per Status
            if (outputReport.isEmpty() || outputReport.contains("statsChart")) {
                jsonResponse.put("statsChart", generateStats(request, testCaseExecutions, statusFilter, countryFilter, true));
            }
            // BugTracker Recap
            if (outputReport.isEmpty() || outputReport.contains("bugTrackerStat")) {
                jsonResponse.put("bugTrackerStat", generateBugStats(request, testCaseExecutions, statusFilter, countryFilter));
            }
            // Labels Stats
            if (outputReport.isEmpty() || outputReport.contains("labelStat")) {
                jsonResponse.put("labelStat", generateLabelStats(appContext, request, testCaseExecutions, statusFilter, countryFilter, testCaseLabelScopeList));
            }
            if (!outputReport.isEmpty()) {
                //currently used to optimize the homePage
                if (outputReport.contains("totalStatsCharts") && !outputReport.contains("statsChart")) {
                    jsonResponse.put("statsChart", generateStats(request, testCaseExecutions, statusFilter, countryFilter, false));
                }
                //currently used to optimize the homePage
                if (outputReport.contains("resendTag")) {
                    jsonResponse.put("tag", Tag);
                }
            }
            Tag mytag = tagService.convert(tagService.readByKey(Tag));
            if (mytag != null) {
                JSONObject tagJSON = convertTagToJSONObject(mytag);
                jsonResponse.put("tagObject", tagJSON);
                jsonResponse.put("tagDuration", (mytag.getDateEndQueue().getTime() - mytag.getDateCreated().getTime()) / 60000);
            }

            answer.setItem(jsonResponse);
            answer.setResultMessage(answer.getResultMessage().resolveDescription("ITEM", "Tag Statistics").resolveDescription("OPERATION", "Read"));

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (ParseException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (CerberusException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (JSONException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (Exception ex) {
            LOG.error("Error on main call : " + ex, ex);
        }
    }

    private JSONObject testCaseExecutionToJSONObject(TestCaseExecution testCaseExecution) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ID", String.valueOf(testCaseExecution.getId()));
        result.put("QueueID", String.valueOf(testCaseExecution.getQueueID()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getEnvironment()));
        result.put("Start", testCaseExecution.getStart());
        result.put("End", testCaseExecution.getEnd());
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseExecution.getCountry()));
        result.put("RobotDecli", JavaScriptUtils.javaScriptEscape(testCaseExecution.getRobotDecli()));
        result.put("ManualExecution", JavaScriptUtils.javaScriptEscape(testCaseExecution.getManualExecution()));
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseExecution.getStatus()));
        result.put("NbExecutions", String.valueOf(testCaseExecution.getNbExecutions()));
        result.put("previousExeId", testCaseExecution.getPreviousExeId());
        if (testCaseExecution.getPreviousExeStatus() != null) {
            result.put("previousExeControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getPreviousExeStatus()));
        }
        if (testCaseExecution.getQueueState() != null) {
            result.put("QueueState", JavaScriptUtils.javaScriptEscape(testCaseExecution.getQueueState()));
        }

        String bugId;
        String comment;
        String function;
        String shortDesc;
        if ((testCaseExecution.getTestCaseObj() != null) && (testCaseExecution.getTestCaseObj().getTest() != null)) {
            if (testCaseExecution.getApplicationObj() != null && testCaseExecution.getApplicationObj().getBugTrackerUrl() != null
                    && !"".equals(testCaseExecution.getApplicationObj().getBugTrackerUrl()) && testCaseExecution.getTestCaseObj().getBugID() != null) {
                bugId = testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", testCaseExecution.getTestCaseObj().getBugID());
                bugId = new StringBuffer("<a href='")
                        .append(bugId)
                        .append("' target='reportBugID'>")
                        .append(testCaseExecution.getTestCaseObj().getBugID())
                        .append("</a>")
                        .toString();
            } else {
                bugId = testCaseExecution.getTestCaseObj().getBugID();
            }
            comment = JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCaseObj().getComment());
            function = JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCaseObj().getFunction());
            shortDesc = testCaseExecution.getTestCaseObj().getDescription();
        } else {
            bugId = "";
            comment = "";
            function = "";
            shortDesc = "";
        }
        result.put("BugID", bugId);

        result.put("Priority", JavaScriptUtils.javaScriptEscape(String.valueOf(testCaseExecution.getTestCaseObj().getPriority())));
        result.put("Comment", comment);
        result.put("Function", function);
        result.put("ShortDescription", shortDesc);

        result.put("Application", JavaScriptUtils.javaScriptEscape(testCaseExecution.getApplication()));

        List<JSONObject> testCaseDep = new ArrayList<>();

        if (testCaseExecution.getTestCaseExecutionQueueDepList() != null) {
            for (TestCaseExecutionQueueDep tce : testCaseExecution.getTestCaseExecutionQueueDepList()) {
                JSONObject obj = new JSONObject();
                obj.put("test", tce.getDepTest());
                obj.put("testcase", tce.getDepTestCase());
                testCaseDep.add(obj);
            }
        }
        result.put("TestCaseDep", testCaseDep);

        return result;
    }

    private JSONObject getStatusList(HttpServletRequest request) {
        JSONObject statusList = new JSONObject();

        try {
            statusList.put("OK", ParameterParserUtil.parseStringParam(request.getParameter("OK"), "off"));
            statusList.put("KO", ParameterParserUtil.parseStringParam(request.getParameter("KO"), "off"));
            statusList.put("NA", ParameterParserUtil.parseStringParam(request.getParameter("NA"), "off"));
            statusList.put("NE", ParameterParserUtil.parseStringParam(request.getParameter("NE"), "off"));
            statusList.put("WE", ParameterParserUtil.parseStringParam(request.getParameter("WE"), "off"));
            statusList.put("PE", ParameterParserUtil.parseStringParam(request.getParameter("PE"), "off"));
            statusList.put("FA", ParameterParserUtil.parseStringParam(request.getParameter("FA"), "off"));
            statusList.put("CA", ParameterParserUtil.parseStringParam(request.getParameter("CA"), "off"));
            statusList.put("QU", ParameterParserUtil.parseStringParam(request.getParameter("QU"), "off"));
            statusList.put("QE", ParameterParserUtil.parseStringParam(request.getParameter("QE"), "off"));
        } catch (JSONException ex) {
            LOG.error("Error on getStatusList : " + ex, ex);
        }

        return statusList;
    }

    private JSONObject getCountryList(HttpServletRequest request, ApplicationContext appContext) {
        JSONObject countryList = new JSONObject();
        try {
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            AnswerList answer = invariantService.readByIdname("COUNTRY"); //TODO: handle if the response does not turn ok
            for (Invariant country : (List<Invariant>) answer.getDataList()) {
                countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
            }
        } catch (JSONException ex) {
            LOG.error("Error on getCountryList : " + ex, ex);
        }

        return countryList;
    }

    private JSONObject generateTestCaseExecutionTable(ApplicationContext appContext, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, List<TestCaseLabel> testCaseLabelList) {
        JSONObject testCaseExecutionTable = new JSONObject();
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<String, JSONObject>();

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            try {
                String controlStatus = testCaseExecution.getControlStatus();

                // We check is Country and status is inside the fitered values.
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                    JSONObject executionJSON = testCaseExecutionToJSONObject(testCaseExecution);
                    String execKey = testCaseExecution.getEnvironment() + " " + testCaseExecution.getCountry() + " " + testCaseExecution.getRobotDecli();
                    String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
                    JSONObject execTab = new JSONObject();
                    JSONObject ttcObject = new JSONObject();

                    if (ttc.containsKey(testCaseKey)) {
                        // We add an execution entry into the testcase line.
                        ttcObject = ttc.get(testCaseKey);
                        execTab = ttcObject.getJSONObject("execTab");
                        execTab.put(execKey, executionJSON);
                        ttcObject.put("execTab", execTab);
                        Integer nbExeTot = (Integer) ttcObject.get("NbExecutionsTotal");
                        nbExeTot += testCaseExecution.getNbExecutions() - 1;
                        ttcObject.put("NbExecutionsTotal", nbExeTot);

                    } else {
                        // We add a new testcase entry (with The current execution).
                        ttcObject.put("test", testCaseExecution.getTest());
                        ttcObject.put("testCase", testCaseExecution.getTestCase());
                        ttcObject.put("shortDesc", testCaseExecution.getDescription());
                        ttcObject.put("status", testCaseExecution.getStatus());
                        ttcObject.put("application", testCaseExecution.getApplication());
                        boolean testExist = ((testCaseExecution.getTestCaseObj() != null) && (testCaseExecution.getTestCaseObj().getTest() != null));
                        if (testExist) {
                            ttcObject.put("function", testCaseExecution.getTestCaseObj().getFunction());
                            ttcObject.put("priority", testCaseExecution.getTestCaseObj().getPriority());
                            ttcObject.put("comment", testCaseExecution.getTestCaseObj().getComment());
                            if ((testCaseExecution.getApplicationObj() != null) && (testCaseExecution.getApplicationObj().getBugTrackerUrl() != null) && (testCaseExecution.getTestCaseObj().getBugID() != null)) {
                                ttcObject.put("bugId", new JSONObject("{\"bugId\":\"" + testCaseExecution.getTestCaseObj().getBugID() + "\",\"bugTrackerUrl\":\"" + testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", testCaseExecution.getTestCaseObj().getBugID()) + "\"}"));
                            } else {
                                ttcObject.put("bugId", new JSONObject("{\"bugId\":\"\",\"bugTrackerUrl\":\"\"}"));
                            }
                        } else {
                            ttcObject.put("function", "");
                            ttcObject.put("priority", 0);
                            ttcObject.put("comment", "");
                            ttcObject.put("bugId", new JSONObject("{\"bugId\":\"\",\"bugTrackerUrl\":\"\"}"));
                        }
                        // Flag that report if test case still exist.
                        ttcObject.put("testExist", testExist);

                        // Adding nb of execution on retry.
                        ttcObject.put("NbExecutionsTotal", (testCaseExecution.getNbExecutions() - 1));

                        execTab.put(execKey, executionJSON);
                        ttcObject.put("execTab", execTab);

                        /**
                         * Iterate on the label retrieved and generate HashMap
                         * based on the key Test_TestCase
                         */
                        LinkedHashMap<String, JSONArray> testCaseWithLabel = new LinkedHashMap();
                        for (TestCaseLabel label : (List<TestCaseLabel>) testCaseLabelList) {
                            if (Label.TYPE_STICKER.equals(label.getLabel().getType())) { // We only display STICKER Type Label in Reporting By Tag Page..
                                String key = label.getTest() + "_" + label.getTestcase();

                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
                                if (testCaseWithLabel.containsKey(key)) {
                                    testCaseWithLabel.get(key).put(jo);
                                } else {
                                    testCaseWithLabel.put(key, new JSONArray().put(jo));
                                }
                            }
                        }
                        ttcObject.put("labels", testCaseWithLabel.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase()));
                    }
                    ttc.put(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase(), ttcObject);

                    JSONObject column = new JSONObject();
                    column.put("country", testCaseExecution.getCountry());
                    column.put("environment", testCaseExecution.getEnvironment());
                    column.put("robotDecli", testCaseExecution.getRobotDecli());
                    columnMap.put(testCaseExecution.getRobotDecli() + "_" + testCaseExecution.getCountry() + "_" + testCaseExecution.getEnvironment(), column);

                }
                Map<String, JSONObject> treeMap = new TreeMap<String, JSONObject>(columnMap);
                testCaseExecutionTable.put("tableContent", ttc.values());
                testCaseExecutionTable.put("iTotalRecords", ttc.size());
                testCaseExecutionTable.put("iTotalDisplayRecords", ttc.size());
                testCaseExecutionTable.put("tableColumns", treeMap.values());
            } catch (JSONException ex) {
                LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
            } catch (Exception ex) {
                LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
            }
        }
        return testCaseExecutionTable;
    }

    private JSONObject generateFunctionChart(List<TestCaseExecution> testCaseExecutions, String tag, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {
        JSONObject jsonResult = new JSONObject();
        Map<String, JSONObject> axisMap = new HashMap<String, JSONObject>();
        String globalStart = "";
        String globalEnd = "";
        long globalStartL = 0;
        long globalEndL = 0;
        String globalStatus = "Finished";

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key;
            JSONObject control = new JSONObject();
            JSONObject function = new JSONObject();

            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {
                if (testCaseExecution.getTestCaseObj() != null && testCaseExecution.getTestCaseObj().getFunction() != null && !"".equals(testCaseExecution.getTestCaseObj().getFunction())) {
                    key = testCaseExecution.getTestCaseObj().getFunction();
                } else {
                    key = testCaseExecution.getTest();
                }

                controlStatus = testCaseExecution.getControlStatus();

                control.put("value", 1);
                control.put("color", getColor(controlStatus));
                control.put("label", controlStatus);
                function.put("name", key);

                if (axisMap.containsKey(key)) {
                    function = axisMap.get(key);
                    if (function.has(controlStatus)) {
                        int prec = function.getJSONObject(controlStatus).getInt("value");
                        control.put("value", prec + 1);
                    }
                }
                function.put(controlStatus, control);
                axisMap.put(key, function);
            }
            if (testCaseExecution.getStart() != 0) {
                if ((globalStartL == 0) || (globalStartL > testCaseExecution.getStart())) {
                    globalStartL = testCaseExecution.getStart();
                    globalStart = String.valueOf(new Date(testCaseExecution.getStart()));
                }
            }
            if (!testCaseExecution.getControlStatus().equalsIgnoreCase("PE") && testCaseExecution.getEnd() != 0) {
                if ((globalEndL == 0) || (globalEndL < testCaseExecution.getEnd())) {
                    globalEndL = testCaseExecution.getEnd();
                    globalEnd = String.valueOf(new Date(testCaseExecution.getEnd()));
                }
            }
            if (testCaseExecution.getControlStatus().equalsIgnoreCase("PE")) {
                globalStatus = "Pending...";
            }
        }

        Gson gson = new Gson();
        List<JSONObject> axisList = new ArrayList<>();

        for (Map.Entry<String, JSONObject> entry : axisMap.entrySet()) {
            String key = entry.getKey();
            JSONObject value = entry.getValue();
            axisList.add(value);
        }
        Collections.sort(axisList, new SortExecution());
        jsonResult.put("axis", axisList);
        jsonResult.put("tag", tag);
        jsonResult.put("globalEnd", gson.toJson(new Timestamp(globalEndL)).replace("\"", ""));
        jsonResult.put("globalStart", globalStart);
        jsonResult.put("globalStatus", globalStatus);

        return jsonResult;
    }

    class SortExecution implements Comparator<JSONObject> {
        // Used for sorting in ascending order of 
        // name value. 

        @Override
        public int compare(JSONObject a, JSONObject b) {
            if (a != null && b != null) {
                try {
                    String aS = (String) a.get("name");
                    String bS = (String) b.get("name");
                    return aS.compareToIgnoreCase(bS);
                } catch (JSONException ex) {
                    LOG.error("JSON Error Exception", ex);
                    return 1;
                }
            } else {
                return 1;
            }
        }
    }

    private JSONObject generateStats(HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, boolean splitStats) throws JSONException {

        JSONObject jsonResult = new JSONObject();
        boolean env = request.getParameter("env") != null || !splitStats;
        boolean country = request.getParameter("country") != null || !splitStats;
        boolean robotDecli = request.getParameter("robotDecli") != null || !splitStats;
        boolean app = request.getParameter("app") != null || !splitStats;

        HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<String, SummaryStatisticsDTO>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                StringBuilder key = new StringBuilder();

                key.append((env) ? testCaseExecution.getEnvironment() : "");
                key.append("_");
                key.append((country) ? testCaseExecution.getCountry() : "");
                key.append("_");
                key.append((robotDecli) ? testCaseExecution.getRobotDecli() : "");
                key.append("_");
                key.append((app) ? testCaseExecution.getApplication() : "");

                SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
                stat.setEnvironment(testCaseExecution.getEnvironment());
                stat.setCountry(testCaseExecution.getCountry());
                stat.setRobotDecli(testCaseExecution.getRobotDecli());
                stat.setApplication(testCaseExecution.getApplication());

                statMap.put(key.toString(), stat);
            }
        }

        jsonResult.put("contentTable", getStatByEnvCountryRobotDecli(testCaseExecutions, statMap, env, country, robotDecli, app, statusFilter, countryFilter, splitStats));

        return jsonResult;
    }

    private JSONObject generateBugStats(HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {

        JSONObject jsonResult = new JSONObject();
        SummaryStatisticsBugTrackerDTO stat = new SummaryStatisticsBugTrackerDTO();
        String bugsToReport = "KO,FA";
        stat.setNbExe(1);
        int totalBugReported = 0;
        int totalBugToReport = 0;
        int totalBugToReportReported = 0;
        int totalBugToClean = 0;
        HashMap<String, SummaryStatisticsBugTrackerDTO> statMap = new HashMap<String, SummaryStatisticsBugTrackerDTO>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                String key = "";

                if (bugsToReport.contains(testCaseExecution.getControlStatus())) {
                    totalBugToReport++;
                }
                if ((testCaseExecution.getTestCaseObj() != null) && (!StringUtil.isNullOrEmpty(testCaseExecution.getTestCaseObj().getBugID()))) {
                    key = testCaseExecution.getTestCaseObj().getBugID();
                    stat = statMap.get(key);
                    totalBugReported++;
                    if (stat == null) {
                        stat = new SummaryStatisticsBugTrackerDTO();
                        stat.setNbExe(1);
                        stat.setBugId(testCaseExecution.getTestCaseObj().getBugID());
                        stat.setBugIdURL(testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", testCaseExecution.getTestCaseObj().getBugID()));
                        stat.setExeIdLastStatus(testCaseExecution.getControlStatus());
                        stat.setExeIdFirst(testCaseExecution.getId());
                        stat.setExeIdLast(testCaseExecution.getId());
                        stat.setTestFirst(testCaseExecution.getTest());
                        stat.setTestLast(testCaseExecution.getTest());
                        stat.setTestCaseFirst(testCaseExecution.getTestCase());
                        stat.setTestCaseLast(testCaseExecution.getTestCase());
                    } else {
                        stat.setNbExe(stat.getNbExe() + 1);
                        stat.setExeIdLastStatus(testCaseExecution.getControlStatus());
                        stat.setExeIdLast(testCaseExecution.getId());
                        stat.setTestLast(testCaseExecution.getTest());
                        stat.setTestCaseLast(testCaseExecution.getTestCase());
                    }
                    if (!(bugsToReport.contains(testCaseExecution.getControlStatus()))) {
                        totalBugToClean++;
                        stat.setToClean(true);
                    } else {
                        totalBugToReportReported++;
                    }
                    statMap.put(key, stat);
                }

            }
        }

        Gson gson = new Gson();
        JSONArray dataArray = new JSONArray();
        for (String key : statMap.keySet()) {
            SummaryStatisticsBugTrackerDTO sumStats = statMap.get(key);
            dataArray.put(new JSONObject(gson.toJson(sumStats)));
        }

        jsonResult.put("BugTrackerStat", dataArray);
        jsonResult.put("totalBugToReport", totalBugToReport);
        jsonResult.put("totalBugToReportReported", totalBugToReportReported);
        jsonResult.put("totalBugReported", totalBugReported);
        jsonResult.put("totalBugToClean", totalBugToClean);

        return jsonResult;
    }

    private JSONObject getStatByEnvCountryRobotDecli(List<TestCaseExecution> testCaseExecutions, HashMap<String, SummaryStatisticsDTO> statMap, boolean env, boolean country, boolean robotDecli, boolean app, JSONObject statusFilter, JSONObject countryFilter, boolean splitStats) throws JSONException {
        SummaryStatisticsDTO total = new SummaryStatisticsDTO();
        total.setEnvironment("Total");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {

            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on") || !splitStats) {
                StringBuilder key = new StringBuilder();

                key.append((env) ? testCaseExecution.getEnvironment() : "");
                key.append("_");
                key.append((country) ? testCaseExecution.getCountry() : "");
                key.append("_");
                key.append((robotDecli) ? testCaseExecution.getRobotDecli() : "");
                key.append("_");
                key.append((app) ? testCaseExecution.getApplication() : "");

                if (statMap.containsKey(key.toString())) {
                    statMap.get(key.toString()).updateStatisticByStatus(testCaseExecution.getControlStatus());
                }
                total.updateStatisticByStatus(testCaseExecution.getControlStatus());
            }
        }
        return extractSummaryData(statMap, total, splitStats);
    }

    private JSONObject extractSummaryData(HashMap<String, SummaryStatisticsDTO> summaryMap, SummaryStatisticsDTO total, boolean splitStats) throws JSONException {
        JSONObject extract = new JSONObject();
        Gson gson = new Gson();
        if (splitStats) {
            JSONArray dataArray = new JSONArray();
            //sort keys
            TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<String, SummaryStatisticsDTO>(summaryMap);
            for (String key : sortedKeys.keySet()) {
                SummaryStatisticsDTO sumStats = summaryMap.get(key);
                //percentage values
                sumStats.updatePercentageStatistics();
                dataArray.put(new JSONObject(gson.toJson(sumStats)));
            }
            extract.put("split", dataArray);
        }
        total.updatePercentageStatistics();
        extract.put("total", new JSONObject(gson.toJson(total)));
        return extract;
    }

    private String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_OK_COL;
        } else if ("KO".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_KO_COL;
        } else if ("FA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_FA_COL;
        } else if ("CA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_CA_COL;
        } else if ("NA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NA_COL;
        } else if ("NE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NE_COL;
        } else if ("WE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_WE_COL;
        } else if ("PE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PE_COL;
        } else if ("QU".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QU_COL;
        } else if ("QE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QE_COL;
        } else {
            color = "#000000";
        }
        return color;
    }

    private JSONObject convertTagToJSONObject(Tag tag) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(tag));
        return result;
    }

    private JSONObject generateLabelStats(ApplicationContext appContext, HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, List<TestCaseLabel> testCaseLabelList) throws JSONException {

        JSONObject jsonResult = new JSONObject();

        labelService = appContext.getBean(LabelService.class);
        TreeNode node;
        JSONArray jsonArraySTICKER = new JSONArray();
        JSONArray jsonArrayREQUIREMENT = new JSONArray();

        AnswerList resp = labelService.readByVarious(new ArrayList<>(), new ArrayList<>(asList(Label.TYPE_STICKER, Label.TYPE_REQUIREMENT)));

        // Building Label inputlist with target layout
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

            HashMap<Integer, TreeNode> inputList = new HashMap();

            for (Label label : (List<Label>) resp.getDataList()) {

                String text = "";

                text += "<span class='label label-primary' style='background-color:" + label.getColor() + "' data-toggle='tooltip' data-labelid='" + label.getId() + "' title='' data-original-title=''>" + label.getLabel() + "</span>";
                text += "<span style='margin-left: 5px; margin-right: 5px;' class=''>" + label.getDescription() + "</span>";

                text += "%STATUSBAR%";
                text += "%COUNTER1TEXT%";
                text += "%COUNTER1WITHCHILDTEXT%";
                text += "%NBNODESWITHCHILDTEXT%";

                // Specific pills
                //text += "<span class='badge badge-pill badge-secondary'>666</span>";
                // Standard pills
                List<String> attributList = new ArrayList<>();
                if (Label.TYPE_REQUIREMENT.equals(label.getType())) {
                    if (!StringUtil.isNullOrEmpty(label.getReqType()) && !"unknown".equalsIgnoreCase(label.getReqType())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getReqType() + "</span>");
                    }
                    if (!StringUtil.isNullOrEmpty(label.getReqStatus()) && !"unknown".equalsIgnoreCase(label.getReqStatus())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getReqStatus() + "</span>");
                    }
                    if (!StringUtil.isNullOrEmpty(label.getReqCriticity()) && !"unknown".equalsIgnoreCase(label.getReqCriticity())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getReqCriticity() + "</span>");
                    }
                }

                // Create Node.
                node = new TreeNode(label.getId() + "-" + label.getSystem() + "-" + label.getLabel(), label.getSystem(), label.getLabel(), label.getId(), label.getParentLabelID(), text, null, null, false);
                node.setCounter1(0);
                node.setCounter1WithChild(0);
                node.setTags(attributList);
                node.setType(label.getType());
                node.setCounter1Text("<span style='background-color:#000000' class='cnt1 badge badge-pill badge-secondary'>%COUNTER1%</span>");
                node.setCounter1WithChildText("<span class='cnt1WC badge badge-pill badge-secondary'>%COUNTER1WITHCHILD%</span>");
                node.setNbNodesText("<span style='background-color:#337ab7' class='nbNodes badge badge-pill badge-primary'>%NBNODESWITHCHILD%</span>");
                node.setLabelObj(label);
                inputList.put(node.getId(), node);
//                    LOG.debug("Label : " + node.getId() + " T : " + node);
            }

            HashMap<String, List<Integer>> testCaseWithLabel1 = new HashMap();
            for (TestCaseLabel label : (List<TestCaseLabel>) testCaseLabelList) {
//                LOG.debug("TCLabel : " + label.getLabel() + " T : " + label.getTest() + " C : " + label.getTestcase() + " Type : " + label.getLabel().getType());
                if ((Label.TYPE_STICKER.equals(label.getLabel().getType()))
                        || (Label.TYPE_REQUIREMENT.equals(label.getLabel().getType()))) {
                    String key = label.getTest() + "_" + label.getTestcase();
                    List<Integer> curLabelIdList = new ArrayList<>();
                    if (testCaseWithLabel1.get(key) != null) {
                        curLabelIdList = testCaseWithLabel1.get(key);
                        curLabelIdList.add(label.getLabelId());
                        testCaseWithLabel1.put(key, curLabelIdList);
//                        LOG.debug("  ADDED");
                    } else {
                        curLabelIdList.add(label.getLabelId());
                        testCaseWithLabel1.put(key, curLabelIdList);
//                        LOG.debug("  ADDED");
                    }
                }
            }

            /**
             * For All execution, get all labels from the test case and add if
             * those labels were in the list add the stats of executions into
             * the counters.
             */
            for (TestCaseExecution testCaseExecution : testCaseExecutions) {
//                    LOG.debug("Exe : " + testCaseExecution.getId() + " T : " + testCaseExecution.getTest() + " C : " + testCaseExecution.getTestCase());
                String controlStatus = testCaseExecution.getControlStatus();
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                    //Get label for current test_testcase
                    List<Integer> labelsForTestCase = testCaseWithLabel1.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase());
                    if (labelsForTestCase != null) {
                        for (Integer integer : labelsForTestCase) {
//                                LOG.debug(" T : " + testCaseExecution.getTest() + " C : " + testCaseExecution.getTestCase() + " T : " + integer);
                            TreeNode curTreenode = inputList.get(integer);
                            if (curTreenode != null) {
//                                    LOG.debug(" K : " + titi.getKey() + " C : " + titi.getCounter1());
                                curTreenode.setCounter1(curTreenode.getCounter1() + 1);
                                curTreenode.setCounter1WithChild(curTreenode.getCounter1WithChild() + 1);
                                switch (testCaseExecution.getControlStatus()) {
                                    case TestCaseExecution.CONTROLSTATUS_OK:
                                        curTreenode.setNbOK(curTreenode.getNbOK() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_KO:
                                        curTreenode.setNbKO(curTreenode.getNbKO() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_FA:
                                        curTreenode.setNbFA(curTreenode.getNbFA() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_NA:
                                        curTreenode.setNbNA(curTreenode.getNbNA() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_NE:
                                        curTreenode.setNbNE(curTreenode.getNbNE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_WE:
                                        curTreenode.setNbWE(curTreenode.getNbWE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_PE:
                                        curTreenode.setNbPE(curTreenode.getNbPE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_QE:
                                        curTreenode.setNbQE(curTreenode.getNbQE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_QU:
                                        curTreenode.setNbQU(curTreenode.getNbQU() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_CA:
                                        curTreenode.setNbCA(curTreenode.getNbCA() + 1);
                                        break;
                                }
                                inputList.put(curTreenode.getId(), curTreenode);
                            }
                        }
                    }
                }
            }

            // Build Tres.
            List<TreeNode> finalList;
            jsonArraySTICKER = new JSONArray();
            jsonArrayREQUIREMENT = new JSONArray();
            finalList = labelService.hierarchyConstructor(inputList);

            for (TreeNode treeNode : finalList) {
                if (treeNode.getCounter1WithChild() > 0) {
                    if (Label.TYPE_STICKER.equals(treeNode.getType())) {
                        jsonArraySTICKER.put(treeNode.toJson());
                    } else {
                        jsonArrayREQUIREMENT.put(treeNode.toJson());
                    }
                }
            }

        }

        jsonResult.put("labelTreeSTICKER", jsonArraySTICKER);
        jsonResult.put("labelTreeREQUIREMENT", jsonArrayREQUIREMENT);

        return jsonResult;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
