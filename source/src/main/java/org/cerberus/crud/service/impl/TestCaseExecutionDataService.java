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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionDataService implements ITestCaseExecutionDataService {

    @Autowired
    ITestCaseExecutionDataDAO testCaseExecutionDataDao;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlExecutionService.class);

    @Override
    public TestCaseExecutionData readByKey(long id, String property, int index) throws CerberusException {
        return testCaseExecutionDataDao.readByKey(id, property, index);
    }

    @Override
    public List<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        return testCaseExecutionDataDao.readByIdByCriteria(id, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public TestCaseExecutionData readLastCacheEntry(String system, String environment, String country, String property, int cacheExpire) throws CerberusException {
        return testCaseExecutionDataDao.readLastCacheEntry(system, environment, country, property, cacheExpire);
    }

    @Override
    public List<TestCaseExecutionData> readById(long id) throws CerberusException {
        return testCaseExecutionDataDao.readByIdByCriteria(id, 0, 0, "exd.id", "asc", null, null);
    }

    @Override
    public List<TestCaseExecutionData> readByIdWithDependency(long id) throws CerberusException {
        List<TestCaseExecutionData> data = this.readByIdByCriteria(id, 0, 0, "exd.property", "asc", null, null);

        for (TestCaseExecutionData tcsace : data) {
            AnswerList<TestCaseExecutionFile> files = testCaseExecutionFileService.readByVarious(id, tcsace.getProperty() + "-" + tcsace.getIndex());
            tcsace.setFileList(files.getDataList());
        }

        return data;

    }

    @Override
    public boolean exist(long id, String property, int index) throws CerberusException {
        return readByKey(id, property, index) != null;
    }

    @Override
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) throws CerberusException {
        return testCaseExecutionDataDao.getPastValuesOfProperty(id, propName, test, testCase, build, environment, country);
    }

    @Override
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) throws CerberusException {
        return testCaseExecutionDataDao.getInUseValuesOfProperty(id, propName, environment, country, timeoutInSecond);
    }

    @Override
    public void create(TestCaseExecutionData object) throws CerberusException {
        testCaseExecutionDataDao.create(object);
    }

    @Override
    public void delete(TestCaseExecutionData object) throws CerberusException {
        testCaseExecutionDataDao.delete(object);
    }

    @Override
    public void update(TestCaseExecutionData object) throws CerberusException {
        testCaseExecutionDataDao.update(object);
    }

    @Override
    public TestCaseExecutionData convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseExecutionData) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionData> convert(AnswerList<TestCaseExecutionData> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseExecutionData>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void save(TestCaseExecutionData object) throws CerberusException {
        if (this.exist(object.getId(), object.getProperty(), object.getIndex())) {
            update(object);
        } else {
            create(object);
        }
    }

    @Override
    public void loadTestCaseExecutionDataFromDependencies(final TestCaseExecution testCaseExecution) throws CerberusException {

        // We get the full list of ExecutionData from dependencies.
        List<TestCaseExecutionData> testCaseExecutionData = testCaseExecutionDataDao.readTestCaseExecutionDataFromDependencies(testCaseExecution);

        // We then dedup it per property name.
        TreeMap<String, TestCaseExecutionData> newExeDataMap = new TreeMap<>();
        for (TestCaseExecutionData data : testCaseExecutionData) {
            data.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RETRIEVE_BY_DEPENDENCY).resolveDescription("EXEID", String.valueOf(data.getId())));
            data.setId(testCaseExecution.getId());
            if (!StringUtil.isNullOrEmpty(data.getJsonResult())) {
                try {
                    JSONArray array = new JSONArray(data.getJsonResult());
                    List<HashMap<String, String>> libRawData = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        HashMap<String, String> hashJson = new HashMap<>();
                        JSONObject obj = array.getJSONObject(i);
                        Iterator<String> nameItr = obj.keys();
                        while (nameItr.hasNext()) {
                            String name = nameItr.next();
                            hashJson.put(name, obj.getString(name));
                        }
                        libRawData.add(hashJson);
                    }
                    data.setDataLibRawData(libRawData);
                } catch (JSONException ex) {
                    LOG.warn("Exception when converting JSON Object '" + data.getJsonResult() + "' from database", ex);
                    data.setDataLibRawData(null);
                }
            }

            newExeDataMap.put(data.getProperty(), data);
        }

        // And finally set the dedup result to execution object and also record all results to database.
        testCaseExecution.setTestCaseExecutionDataMap(newExeDataMap);
        for (Map.Entry<String, TestCaseExecutionData> entry : newExeDataMap.entrySet()) {
            String key = entry.getKey();
            TestCaseExecutionData value = entry.getValue();
            testCaseExecutionDataDao.create(value);
        }

    }

}
