/*
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
var curMode = "";
/***
 * Open the modal with testcase information.
 * @param {String} test - id of the test to open the modal
 * @param {String} testcase - id of the testcase to open the modal
 * @param {String} mode - mode to open the modal. Can take the values : ADD, DUPLICATE, EDIT
 * @param {String} tab - name of the tab to activate
 * @returns {null}
 */
function openModalTestCase(test, testcase, mode, tab) {
    curMode = mode;

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editTestCaseModal').data("initLabel") === undefined) {
        initModalTestCase();
        $('#editTestCaseModal').data("initLabel", true);
    }
    // Init the Saved data to false.
    $('#editTestCaseModal').data("Saved", false);
    $('#editTestCaseModal').data("testcase", undefined);

    if (!isEmpty(tab)) {
        $('.nav-tabs a[href="#' + tab + '"]').tab('show');
    }

    if (mode === "EDIT") {
        editTestCaseClick(test, testcase);
    } else if (mode === "DUPLICATE") {
        duplicateTestCaseClick(test, testcase);
    } else {
        addTestCaseClick(test, "ADD");
    }

    $('#editTestCaseModalForm #application').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#editTestCaseModal'));

}

function initModalTestCase() {
    var doc = new Doc();

    tinymce.init({
        selector: ".wysiwyg"
    });

    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocOnline("testcase", "TestCase"));
    $("[name='lastModifierField']").html(doc.getDocOnline("testcase", "LastModifier"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='statusField']").html(doc.getDocOnline("testcase", "Status"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='actQAField']").html(doc.getDocOnline("testcase", "activeQA"));
    $("[name='actUATField']").html(doc.getDocOnline("testcase", "activeUAT"));
    $("[name='actUATField']").html(doc.getDocOnline("testcase", "activeUAT"));
    $("[name='actProdField']").html(doc.getDocOnline("testcase", "activePROD"));
    $("[name='shortDescField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='behaviorOrValueExpectedField']").html(doc.getDocOnline("testcase", "BehaviorOrValueExpected"));
    $("[name='shortDescField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
    $("[name='creatorField']").html(doc.getDocOnline("testcase", "Creator"));
    $("[name='implementerField']").html(doc.getDocOnline("testcase", "Implementer"));
    $("[name='typeField']").html(doc.getDocOnline("invariant", "Type"));
    $("[name='priorityField']").html(doc.getDocOnline("invariant", "PRIORITY"));
    $("[name='countryListLabel']").html(doc.getDocOnline("testcase", "countryListLabel"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='tcDateCreaField']").html(doc.getDocOnline("testcase", "TCDateCrea"));
    $("[name='activeField']").html(doc.getDocOnline("testcase", "TcActive"));
    $("[name='fromSprintField']").html(doc.getDocOnline("testcase", "FromBuild"));
    $("[name='fromRevField']").html(doc.getDocOnline("testcase", "FromRev"));
    $("[name='toSprintField']").html(doc.getDocOnline("testcase", "ToBuild"));
    $("[name='toRevField']").html(doc.getDocOnline("testcase", "ToRev"));
    $("[name='targetSprintField']").html(doc.getDocOnline("testcase", "TargetBuild"));
    $("[name='targetRevField']").html(doc.getDocOnline("testcase", "TargetRev"));
    $("[name='conditionOperField']").html(doc.getDocOnline("testcase", "ConditionOper"));
    $("[name='conditionVal1Field']").html(doc.getDocOnline("testcase", "ConditionVal1"));
    $("[name='conditionVal2Field']").html(doc.getDocOnline("testcase", "ConditionVal2"));
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("[name='versionActivation']").html(doc.getDocOnline("testcase", "versionActivation"));
    $("[name='activationConditions']").html(doc.getDocOnline("testcase", "activationConditions"));
    $("[name='robotConstraints']").html(doc.getDocOnline("testcase", "robotConstraints"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    //TABs
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='testCaseParameterField']").html(doc.getDocLabel("page_testcaselist", "testCaseParameter"));
    $("[name='activationCriteriaField']").html(doc.getDocLabel("page_testcaselist", "activationCriteria"));
    // Tracability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));
    $("[name='testcaseversionField']").html(doc.getDocOnline("testcase", "TestCaseVersion"));

    displayInvariantList("group", "GROUP", false);
    displayInvariantList("status", "TCSTATUS", false);
    displayInvariantList("priority", "PRIORITY", false);
    displayInvariantList("conditionOper", "TESTCASECONDITIONOPER", false);
    $('[name="origin"]').append('<option value="All">All</option>');
    displayInvariantList("active", "TCACTIVE", false);
    displayInvariantList("activeQA", "TCACTIVE", false);
    displayInvariantList("activeUAT", "TCACTIVE", false);
    displayInvariantList("activeProd", "TCACTIVE", false);

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

    var availableUserAgent = getInvariantArray("USERAGENT", false);
    $('#editTestCaseModal').find("#userAgent").autocomplete({
        source: availableUserAgent
    });
    var availableScreenSize = getInvariantArray("SCREENSIZE", false);
    $('#editTestCaseModal').find("#screenSize").autocomplete({
        source: availableScreenSize
    });
    var availableFunctions = getInvariantArray("FUNCTION", false);
    $('#editTestCaseModal').find("#function").autocomplete({
        source: availableFunctions
    });
    $("#select_all").change(function () {  //"select all" change
        $("#countryList input").prop('checked', $(this).prop("checked")); //change all ".checkbox" checked status
    });

    $("#addTestCaseDependencyButton").click(function () {

        var test = $("#selectTest").val();
        var testCase = $("#selectTestCase").val();
        var testCaseTxt = $("#selectTestCase option:selected").text();

        var indexTest = $("#selectTest").prop('selectedIndex')
        var indexTestCase = $("#selectTestCase").prop('selectedIndex')

        if ($('#' + getHtmlIdForTestCase(test, testCase)).length > 0) {
            showMessage(new Message("KO", 'Test case is already added'), $('#editTestCaseModal'));
        } else if (indexTest === 0 || indexTestCase === 0) {
            showMessage(new Message("KO", 'Select a test case'), $('#editTestCaseModal'));
        } else {
            addHtmlForDependencyLine(0, test, testCase, testCaseTxt, true, "")
        }
    })
}

function addHtmlForDependencyLine(id, test, testCase, testCaseTxt, activate, description) {
    let checked = "";
    if (activate)
        checked = "checked";
    $("#depenencyTable").append(
            '<tr role="row" class="odd" id="' + getHtmlIdForTestCase(test, testCase) + '"  test="' + test + '" testcase="' + testCase + '" testcaseid="' + id + '">' +
            '<td class="sorting_1" style="width: 100px;">' +
            '<div class="center btn-group">' +
            '<button id="removeTestparameter" onclick="removeTestCaseDependency(\'' + test + '\',\'' + testCase + '\');" class="removeTestparameter btn btn-default btn-xs margin-right5" name="removeTestparameter" title="Remove Test Case Dependency" type="button">' +
            '<span class="glyphicon glyphicon-trash"></span>' +
            '</button>' +
            '</div>' +
            '</td>' +
            '<td>' + test + ' - ' + testCaseTxt + '</td>' +
            '<td style="width: 100px;">  <input type="checkbox"  name="activate" ' + checked + '/></td>' +
            '<td>  <input class="form-control input-sm" name="description" value="' + description + '"/></td>' +
            '</tr>'
            );
}

function getHtmlIdForTestCase(test, testCase) {
    return (test + '-' + testCase).replace(/ /g, '_').replace(/\./g, '_').replace(/\:/g, '_');
}

function removeTestCaseDependency(test, testCase) {
    $('#' + getHtmlIdForTestCase(test, testCase)).remove();
}


/***
 * Open the modal with testcase information.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @returns {null}
 */
function editTestCaseClick(test, testCase) {

    $("#editTestCaseButton").off("click");
    $("#editTestCaseButton").click(function () {
        confirmTestCaseModalHandler("EDIT");
    });

    $('#editTestCaseButton').attr('class', 'btn btn-primary');
    $('#editTestCaseButton').removeProp('hidden');
    $('#duplicateTestCaseButton').attr('class', '');
    $('#duplicateTestCaseButton').attr('hidden', 'hidden');
    $('#addTestCaseButton').attr('class', '');
    $('#addTestCaseButton').attr('hidden', 'hidden');


    $("#originalTest").prop("value", test);
    $("#originalTestCase").prop("value", testCase);
    $("#testCase").prop("value", testCase);

    // In Edit TestCase form, if we change the test, we get the latest testcase from that test.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(test, "editTestCaseModalForm");
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey(test, testCase);
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");
    $('#editTestCaseModalForm input[name="testCase"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey(test, testCase);
    });
    feedTestCaseModal(test, testCase, "editTestCaseModal", "EDIT");
}

function displayWarningOnChangeTestCaseKey(test, testCase) {
    // Compare with original value in order to display the warning message.
    let old1 = $("#originalTest").val();
    let old2 = $("#originalTestCase").val();
    let new1 = $('#editTestCaseModalForm select[name="test"]').val();
    let new2 = $('#editTestCaseModalForm input[name="testCase"]').val();

    if ((old1 !== new1) || (old2 !== new2)) {
        var localMessage = new Message("WARNING", "If you rename that test case, it will loose the corresponding execution historic.");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else {
        clearResponseMessage($('#editTestCaseModal'));
    }
}

/***
 * Open the modal with testcase information.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @returns {null}
 */
function duplicateTestCaseClick(test, testCase) {

    $("#duplicateTestCaseButton").off("click");
    $("#duplicateTestCaseButton").click(function () {
        confirmTestCaseModalHandler("DUPLICATE");
    });

    $('#editTestCaseButton').attr('class', '');
    $('#editTestCaseButton').attr('hidden', 'hidden');
    $('#duplicateTestCaseButton').attr('class', 'btn btn-primary');
    $('#duplicateTestCaseButton').removeProp('hidden');
    $('#addTestCaseButton').attr('class', '');
    $('#addTestCaseButton').attr('hidden', 'hidden');

    // In Duplicate TestCase form, if we change the test, we get the latest testcase from that test.
//    $('#editTestCaseModalForm select[name="test"]').off("change");
//    $('#editTestCaseModalForm select[name="test"]').change(function () {
//        feedTestCaseField(null, "editTestCaseModalForm");
//    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(test, "editTestCaseModalForm");
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");

    feedTestCaseModal(test, testCase, "editTestCaseModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @param {String} defaultTest - optionaly define the test context to pick for creating the new testcase.
 * @returns {null}
 */
function addTestCaseClick(defaultTest) {

    $("#addTestCaseButton").off("click");
    $("#addTestCaseButton").click(function () {
        confirmTestCaseModalHandler("ADD");
    });

    $('#editTestCaseButton').attr('class', '');
    $('#editTestCaseButton').attr('hidden', 'hidden');
    $('#duplicateTestCaseButton').attr('class', '');
    $('#duplicateTestCaseButton').attr('hidden', 'hidden');
    $('#addTestCaseButton').attr('class', 'btn btn-primary');
    $('#addTestCaseButton').removeProp('hidden');

//    $('#editTestCaseModalForm select[name="test"]').off("change");
//    $('#editTestCaseModalForm select[name="test"]').change(function () {
//        feedTestCaseField(null, "editTestCaseModalForm");
//    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(defaultTest, "editTestCaseModalForm");
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");

    feedNewTestCaseModal("editTestCaseModal", defaultTest);
}

/***
 * Feed the testcase field inside modalForm modal with a new occurence value 
 * for the given test. used when create or duplicate a new testcase.
 * @param {String} test - test used to calculate the new testcase value.
 * @param {String} modalForm - modal name where the testcase will be filled.
 * @returns {null}
 */
function feedTestCaseField(test, modalForm) {
//    console.info("feed Test Case. " + test + " mode : " + curMode);
    var trigNewTestCase = true;
// Predefine the testcase value.
    if (curMode !== "EDIT") {
        trigNewTestCase = true;
        let new1 = $('#editTestCaseModalForm select[name="test"]').val();
        test = new1;

    } else {
        trigNewTestCase = false;
        let old1 = $("#originalTest").val();
        let new1 = $('#editTestCaseModalForm select[name="test"]').val();
        if (test !== new1) {
            test = new1;
            trigNewTestCase = true;
        } else {

            trigNewTestCase = false;
        }

    }

    if (trigNewTestCase) {
        $.ajax({
            url: "ReadTestCase",
            method: "GET",
            data: {test: encodeURIComponent(test), getMaxTC: true},
            dataType: "json",
            success: function (data) {
                var testCaseNumber = data.maxTestCase + 1;
                var tcnumber;

                if (testCaseNumber < 10) {
                    tcnumber = "000" + testCaseNumber.toString() + "A";
                } else if (testCaseNumber >= 10 && testCaseNumber < 99) {
                    tcnumber = "00" + testCaseNumber.toString() + "A";
                } else if (testCaseNumber >= 100 && testCaseNumber < 999) {
                    tcnumber = "0" + testCaseNumber.toString() + "A";
                } else if (testCaseNumber >= 1000) {
                    tcnumber = testCaseNumber.toString() + "A";
                } else {
                    tcnumber = "0001A";
                }
                $('#' + modalForm + ' [name="testCase"]').val(tcnumber);
            },
            error: showUnexpectedError
        });
    }

}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmTestCaseModalHandler(mode) {
    clearResponseMessage($('#editTestCaseModal'));

    var formEdit = $('#editTestCaseModalForm');

    var nameElement = formEdit.find("#application");
    var nameElementEmpty = nameElement.prop("value") === '';

    var testElement = formEdit.find("#test");
    var testElementInvalid = testElement.prop("value").search("&");
    var testElementEmpty = testElement.prop("value") === '';

    var testIdElement = formEdit.find("#testCase");
    var testIdElementInvalid = testIdElement.prop("value").search("&");
    var testIdElementEmpty = testIdElement.prop("value") === '';

    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testElementInvalid != -1) {
        var localMessage = new Message("danger", "The test name cannot contains the symbol : &");
        // only the Test label will be put in red
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testIdElementInvalid != -1) {
        var localMessage = new Message("danger", "The testcase id name cannot contains the symbol : &");
        // only the TestId label will be put in red
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the test!");
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testIdElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the Testcase Id!");
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verify if all mandatory fields are not empty and valid
    if (nameElementEmpty || testElementInvalid != -1 || testIdElementInvalid != -1 || testElementEmpty || testIdElementEmpty)
        return;

    tinyMCE.triggerSave();

    showLoaderInModal('#editTestCaseModal');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#test").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateTestCase";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateTestCase";
    }

    // Getting Data from Country List
    var countryList = $("#countryList input");
    var table_country = [];
    for (var i = 0; i < countryList.length; i++) {
        if (countryList[i].checked === true) {
            var countryValue = {
                country: $(countryList[i]).attr("name"),
                toDelete: false
            }
        } else {
            countryValue = {
                country: $(countryList[i]).attr("name"),
                toDelete: true
            }
        }
        table_country.push(countryValue)
    }

    // Getting Data from Label List
//    var table2 = $("#editTestCaseModal input[name=labelid]:checked");
//    var table_label = [];
//    for (var i = 0; i < table2.length; i++) {
//        var newLabel1 = {
//            labelId: $(table2[i]).val(),
//            toDelete: false
//        };
//        table_label.push(newLabel1);
//    }

    var table_label = [];
    var table2 = $('#selectLabelS').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }
    var table2 = $('#selectLabelR').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }
    var table2 = $('#selectLabelB').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }


    // Getting Dependency data
    let testcaseDependency = []
    $("#depenencyTable").find("tr")
            .each((t, v) =>
                testcaseDependency.push(
                        {id: $(v).attr("testcaseid"), test: $(v).attr("test"), testcase: $(v).attr("testcase"), description: $(v).find("[name='description']").val(), active: $(v).find("[name='activate']").is(":checked")}
                )
            )


    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestCaseModal');
    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            test: data.test,
            testCase: data.testCase,
            originalTest: data.originalTest,
            originalTestCase: data.originalTestCase,
            active: data.active,
            activeProd: data.activeProd,
            activeQA: data.activeQA,
            activeUAT: data.activeUAT,
            application: data.application,
            behaviorOrValueExpected: data.behaviorOrValueExpected,
            bugId: data.bugId,
            comment: data.comment,
            fromRev: data.fromRev,
            fromSprint: data.fromSprint,
            group: data.group,
            implementer: data.implementer,
            origin: data.origin,
            priority: data.priority,
            project: data.project,
            refOrigin: data.refOrigin,
            shortDesc: data.shortDesc,
            status: data.status,
            targetRev: data.targetRev,
            targetSprint: data.targetSprint,
            conditionOper: data.conditionOper,
            conditionVal1: data.conditionVal1,
            conditionVal2: data.conditionVal2,
            ticket: data.ticket,
            toRev: data.toRev,
            toSprint: data.toSprint,
            userAgent: data.userAgent,
            screenSize: data.screenSize,
            labelList: JSON.stringify(table_label),
            countryList: JSON.stringify(table_country),
            testcaseDependency: JSON.stringify(testcaseDependency)},
        success: function (dataMessage) {
            hideLoaderInModal('#editTestCaseModal');
            if (getAlertType(dataMessage.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(false);
                $('#editTestCaseModal').data("Saved", true);
                $('#editTestCaseModal').data("testcase", data);
                $('#editTestCaseModal').modal('hide');
                showMessage(dataMessage);
            } else {
                showMessage(dataMessage, $('#editTestCaseModal'));
            }
        },
        error: showUnexpectedError
    });

}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} modalId - Id of the modal to feed.
 * @param {String} defaultTest - default test to selected.
 * @returns {null}
 */
function feedNewTestCaseModal(modalId, defaultTest) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    appendBuildRevListOnTestCase(getUser().defaultSystem, undefined);

    feedTestCaseData(undefined, modalId, "ADD", true, defaultTest);
    // Labels
    loadLabel(undefined, undefined, "#selectLabel");
    //Application Combo
    appendApplicationList(undefined, undefined);

    formEdit.modal('show');
}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @param {String} modalId - type selected
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedTestCaseModal(test, testCase, modalId, mode) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);


    var jqxhr = $.getJSON("ReadTestCase", "test=" + encodeURIComponent(test) + "&testCase=" + encodeURIComponent(testCase));
    $.when(jqxhr).then(function (data) {

        var testCase = data.contentTable;

        var appInfo = $.getJSON("ReadApplication", "application=" + encodeURIComponent(testCase.application));

        $.when(appInfo).then(function (appData) {
            var currentSys = getUser().defaultSystem;
            var t = appData.contentTable;
            var bugTrackerUrl = t.bugTrackerUrl;

            // Loading build and revision various combos.
            appendBuildRevListOnTestCase(t.system, testCase);
            // Title of the label list.
            $("#labelField").html("Labels from system : " + t.system);
            // Loading the label list from aplication of the testcase.
            loadLabel(testCase.labelList, t.system, "#selectLabel", undefined, testCase.test, testCase.testCase);
            // Loading application combo from the system of the current application.
            appendApplicationList(testCase.application, t.system);

            var newbugTrackerUrl = "";
            if (testCase.bugID !== "" && bugTrackerUrl) {
                newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", testCase.bugID);
            }
            formEdit.find("#link").prop("href", newbugTrackerUrl).text(testCase.bugID);
            formEdit.find("#link").prop("target", "_blank");

            formEdit.find("#bugId").change(function () {
                var newbugid = formEdit.find("#bugId").val();
                var newbugTrackerUrl = "";
                if (newbugid !== "" && bugTrackerUrl) {
                    newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", newbugid);
                }
                formEdit.find("#link").prop("href", newbugTrackerUrl).text(newbugid);
                formEdit.find("#link").prop("target", "_blank");
            });
        });

        feedTestCaseData(testCase, modalId, mode, data["hasPermissionsUpdate"]);

        formEdit.modal('show');
    });

    fillTestAndTestCaseSelect("#selectTest", "#selectTestCase", undefined, undefined, true)
    $("#selectTest").change(function () {
        fillTestCaseSelect("#selectTestCase", $("#selectTest").val(), undefined, true);
    })


}



function fillTestCaseSelect(selectorTestCaseSelect, test, testcase, allTestCases) {
    var doc = new Doc()
    var system = getSys()
    var url1 = "";
    if (allTestCases) {
        url1 = getUser().systemQuery;
    } else {
        url1 = getUser().defaultSystemsQuery;
    }
    if (test !== null && test !== undefined) {
        $.ajax({
            url: "ReadTestCase?test=" + encodeURIComponent(test) + url1,
            async: true,
            success: function (data) {
                data.contentTable.sort(function (a, b) {
                    var aa = a.testCase.toLowerCase();
                    var bb = b.testCase.toLowerCase();
                    if (aa > bb) {
                        return 1;
                    } else if (aa < bb) {
                        return -1;
                    }
                    return 0;
                });
                $(selectorTestCaseSelect).find('option').remove()

                $(selectorTestCaseSelect).prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_testcase") + "</option>");
                for (var i = 0; i < data.contentTable.length; i++) {
                    $(selectorTestCaseSelect).append("<option value='" + data.contentTable[i].testCase + "'>" + data.contentTable[i].testCase + " - " + data.contentTable[i].description + "</option>")
                }
                if (testcase != null) {
                    $(selectorTestCaseSelect + " option[value='" + testcase + "']").prop('selected', true);
                    window.document.title = "TestCase - " + testcase;
                }

                $(selectorTestCaseSelect).select2({width: '100%'});
            }
        });
    }

}

/**
 * Fill Test and Testcase select,
 * @param test   auto select this test
 * @param testcase  auto select this testcase
 */
function fillTestAndTestCaseSelect(selectorTestSelect, selectorTestCaseSelect, test, testcase, allTestCases) {
    var doc = new Doc()
    var system = getSys()
    $.ajax({
        url: "ReadTest",
        async: true,
        success: function (data) {
            data.contentTable.sort(function (a, b) {
                var aa = a.test.toLowerCase();
                var bb = b.test.toLowerCase();
                if (aa > bb) {
                    return 1;
                } else if (aa < bb) {
                    return -1;
                }
                return 0;
            });

            $(selectorTestSelect).find("option").remove();
            $(selectorTestSelect).prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_test") + "</option>");
            for (var i = 0; i < data.contentTable.length; i++) {
                $(selectorTestSelect).append("<option value='" + data.contentTable[i].test + "'>" + data.contentTable[i].test + " - " + data.contentTable[i].description + "</option>");
            }

            if (test !== null) {
                $(selectorTestSelect + " option[value='" + test + "']").prop('selected', true);
            }

            $(selectorTestSelect).select2({width: "100%"}).next().css("margin-bottom", "7px");
        }
    });

    fillTestCaseSelect(selectorTestCaseSelect, test, testcase, allTestCases)
}


function feedTestCaseData(testCase, modalId, mode, hasPermissionsUpdate, defaultTest) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

//    $('#editTestCaseModal [name="test"]').select2(getComboConfigTest());

    var observer = new MutationObserver(function (mutations, me) {
        var behaviorOrValueExpected = tinyMCE.get('behaviorOrValueExpected');
        if (behaviorOrValueExpected != null) {
            if (isEmpty(testCase)) {
                tinyMCE.get('behaviorOrValueExpected').setContent("");
            } else {
                tinyMCE.get('behaviorOrValueExpected').setContent(testCase.behaviorOrValueExpected);
            }

            me.disconnect()
        }
        return;
    });

    // start observing
    observer.observe(document, {
        childList: true,
        subtree: true
    });

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_edit"));
        appendTestList(testCase.test);
        formEdit.find("#testCase").prop("value", testCase.testCase);
        formEdit.find("#status").prop("value", testCase.status);
        formEdit.find("#usrcreated").prop("value", testCase.usrCreated);
        formEdit.find("#datecreated").prop("value", testCase.dateCreated);
        formEdit.find("#usrmodif").prop("value", testCase.usrModif);
        formEdit.find("#datemodif").prop("value", getDate(testCase.dateModif));
        formEdit.find("#actProd").val(testCase.activePROD);
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        formEdit.find("#actProd").val("N");
        formEdit.find("#status option:nth(0)").attr("selected", "selected"); // We select the 1st entry of the status combobox.
        if (mode === "ADD") {
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_create"));
            appendTestList(defaultTest);
            feedTestCaseField(defaultTest, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        } else { // DUPLICATE
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_duplicate"));
            appendTestList(testCase.test);
            feedTestCaseField(testCase.test, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        }
    }
    if (isEmpty(testCase)) {
        formEdit.find("#originalTest").prop("value", "");
        formEdit.find("#originalTestCase").prop("value", "");
        formEdit.find("#implementer").prop("value", "");
        formEdit.find("#group").val("AUTOMATED");
        formEdit.find("#priority option:nth(0)").attr("selected", "selected");
        formEdit.find("#actQA").val("Y");
        formEdit.find("#actUAT").val("Y");
        formEdit.find("#userAgent").prop("value", "");
        formEdit.find("#screenSize").prop("value", "");
        formEdit.find("#shortDesc").prop("value", "");
        formEdit.find("#active").prop("value", "Y");
        formEdit.find("#bugId").prop("value", "");
        formEdit.find("#conditionOper").prop("value", "always");
        formEdit.find("#conditionVal1").prop("value", "");
        formEdit.find("#conditionVal2").prop("value", "");
        formEdit.find("#comment").prop("value", "");
    } else {
        formEdit.find("#test").prop("value", testCase.test);
        formEdit.find("#originalTest").prop("value", testCase.test);
        formEdit.find("#originalTestCase").prop("value", testCase.testCase);
        formEdit.find("#newTest").prop("value", testCase.test);
        formEdit.find("#implementer").prop("value", testCase.implementer);
        formEdit.find("#tcDateCrea").prop("value", testCase.dateCreated);
        formEdit.find("#group").prop("value", testCase.group);
        formEdit.find("#priority").prop("value", testCase.priority);
        formEdit.find("#actQA").prop("value", testCase.activeQA);
        formEdit.find("#actUAT").prop("value", testCase.activeUAT);
        formEdit.find("#userAgent").prop("value", testCase.userAgent);
        formEdit.find("#screenSize").prop("value", testCase.screenSize);
        formEdit.find("#shortDesc").prop("value", testCase.description);
        formEdit.find("#active").prop("value", testCase.tcActive);
        formEdit.find("#bugId").prop("value", testCase.bugID);
        formEdit.find("#conditionOper").prop("value", testCase.conditionOper);
        formEdit.find("#conditionVal1").prop("value", testCase.conditionVal1);
        formEdit.find("#conditionVal2").prop("value", testCase.conditionVal2);
        formEdit.find("#comment").prop("value", testCase.comment);
        formEdit.find("#testcaseversion").prop("value", testCase.testCaseVersion);
        appendTestCaseDepList(testCase);
    }

    // Authorities

    //We define here the rule that enable or nt the fields depending on if user has the credentials to edit.
    var doBloackAllFields = false;
    if (mode === "EDIT") {
        doBloackAllFields = !(hasPermissionsUpdate);
    } else { // DUPLICATE or ADD
        doBloackAllFields = false;
    }

    if (doBloackAllFields) { // If readonly, we only readonly all fields
        //test case info
        formEdit.find("#test").prop("disabled", "disabled");
        formEdit.find("#testCase").prop("readonly", "readonly");
        formEdit.find("#implementer").prop("readonly", "readonly");
        formEdit.find("#application").prop("disabled", "disabled");
        formEdit.find("#status").prop("disabled", "disabled");
        formEdit.find("#group").prop("disabled", "disabled");
        formEdit.find("#priority").prop("disabled", "disabled");
        formEdit.find("#actQA").prop("disabled", "disabled");
        formEdit.find("#actUAT").prop("disabled", "disabled");
        formEdit.find("#actProd").prop("disabled", "disabled");
        formEdit.find("#userAgent").prop("disabled", "disabled");
        formEdit.find("#screenSize").prop("disabled", "disabled");
        formEdit.find("#shortDesc").prop("readonly", "readonly");
        if (tinyMCE.get('behaviorOrValueExpected') !== null)
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', false);
        formEdit.find("#active").prop("disabled", "disabled");
        formEdit.find("#fromSprint").prop("disabled", "disabled");
        formEdit.find("#fromRev").prop("disabled", "disabled");
        formEdit.find("#toSprint").prop("disabled", "disabled");
        formEdit.find("#toRev").prop("disabled", "disabled");
        formEdit.find("#targetSprint").prop("disabled", "disabled");
        formEdit.find("#targetRev").prop("disabled", "disabled");
        formEdit.find("#conditionOper").prop("disabled", "disabled");
        formEdit.find("#conditionVal1").prop("disabled", "disabled");
        formEdit.find("#conditionVal2").prop("disabled", "disabled");
        formEdit.find("#bugId").prop("readonly", "readonly");
        // feed the country list.
        appendTestCaseCountryList(testCase, true);
        // Save button is hidden.
        $('#editTestCaseButton').attr('class', '');
        $('#editTestCaseButton').attr('hidden', 'hidden');
    } else {
        //test case info
        formEdit.find("#test").removeAttr("disabled");
        formEdit.find("#testCase").removeAttr("readonly");
        formEdit.find("#active").removeProp("disabled");
        formEdit.find("#bugId").removeProp("readonly");
        formEdit.find("#implementer").removeProp("readonly");
        formEdit.find("#application").removeProp("disabled");
        formEdit.find("#status").removeProp("disabled");
        formEdit.find("#group").removeProp("disabled");
        formEdit.find("#priority").removeProp("disabled");
        formEdit.find("#actQA").removeProp("disabled");
        formEdit.find("#actUAT").removeProp("disabled");
        formEdit.find("#actProd").removeProp("disabled");
        formEdit.find("#userAgent").removeProp("disabled");
        formEdit.find("#screenSize").removeProp("disabled");
        formEdit.find("#shortDesc").removeProp("readonly");
        if (tinyMCE.get('behaviorOrValueExpected') !== null)
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', true);
        formEdit.find("#active").removeProp("disabled");
        formEdit.find("#fromSprint").removeProp("disabled");
        formEdit.find("#fromRev").removeProp("disabled");
        formEdit.find("#toSprint").removeProp("disabled");
        formEdit.find("#toRev").removeProp("disabled");
        formEdit.find("#targetSprint").removeProp("disabled");
        formEdit.find("#targetRev").removeProp("disabled");
        formEdit.find("#conditionOper").removeProp("disabled");
        formEdit.find("#conditionVal1").removeProp("disabled");
        formEdit.find("#conditionVal2").removeProp("disabled");
        formEdit.find("#bugId").removeProp("readonly");
        formEdit.find("#comment").removeProp("readonly");
        // feed the country list.
        appendTestCaseCountryList(testCase, false);
    }

}

/***
 * Feed Build and Revision combo on the testcase modal.
 * @param {String} system - system of the testcase.
 * @param {String} editData - testcase data that will be used to feed the values of all combos.
 * @returns {null}
 */
function appendBuildRevListOnTestCase(system, editData) {

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + encodeURIComponent(system) + "&level=1");
    $.when(jqxhr).then(function (data) {
        var fromBuild = $("[name=fromSprint]");
        var toBuild = $("[name=toSprint]");
        var targetBuild = $("[name=targetSprint]");

        fromBuild.empty();
        toBuild.empty();
        targetBuild.empty();

        fromBuild.append($('<option></option>').text("-----").val(""));
        toBuild.append($('<option></option>').text("-----").val(""));
        targetBuild.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("#fromSprint").prop("value", editData.fromBuild);
            formEdit.find("#toSprint").prop("value", editData.toBuild);
            formEdit.find("#targetSprint").prop("value", editData.targetBuild);
        }

    });

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + encodeURIComponent(system) + "&level=2");
    $.when(jqxhr).then(function (data) {
        var fromRev = $("[name=fromRev]");
        var toRev = $("[name=toRev]");
        var targetRev = $("[name=targetRev]");

        fromRev.empty();
        toRev.empty();
        targetRev.empty();

        fromRev.append($('<option></option>').text("-----").val(""));
        toRev.append($('<option></option>').text("-----").val(""));
        targetRev.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("[name=fromRev]").prop("value", editData.fromRev);
            formEdit.find("[name=toRev]").prop("value", editData.toRev);
            formEdit.find("[name=targetRev]").prop("value", editData.targetRev);
        }
    });
}

function appendTestCaseDepList(testCase) {
    $("#depenencyTable").find("tr").remove() // clean the table

    testCase.dependencyList.forEach((dep) =>
        addHtmlForDependencyLine(dep.id, dep.depTest, dep.depTestCase, dep.depTestCase + " - " + dep.depDescription, dep.active, dep.description)
    )
}


function appendTestCaseCountryList(testCase, isReadOnly) {
    $("#countryList label").remove();
    var countryList = $("[name=countryList]");

    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" ' + ' name="' + country + '"/>' + country + '\
                                </label>');
        }
        $("[class='countrycb']").click(function () {
            //uncheck "select all", if one of the listed checkbox item is unchecked
            if (false == $(this).prop("checked")) { //if this item is unchecked
                $("#select_all").prop('checked', false); //change "select all" checked status to false
            }
            //check "select all" if all checkbox items are checked
            if ($("[class='countrycb']:checked").length == $("[class='countrycb']").length) {
                $("#select_all").prop('checked', true);
            }
        });

        if (!(testCase === undefined)) {
            // Init the values from the object value.
            for (var myCountry in testCase.countryList) {
                $("#countryList [name='" + testCase.countryList[myCountry].country + "']").prop("checked", "checked");
            }
        }
        if (testCase === undefined) {
            $("#countryList input").attr('checked', true);
            $("#select_all").attr('checked', true);
        }

        if (isReadOnly) {
            $("#countryList input").attr('disabled', true);
            $("#select_all").attr('disabled', true);
        }
    });
}

/***
 * Build the list of label and flag them from the testcase values..
 * @param {String} labelList - list of labels from the testcase to flag. Label in that list are displayed first. This is optional.
 * @param {String} mySystem - system that will be used in order to load the label list. if not feed, the default system from user will be used.
 * @param {String} myLabelDiv - Reference of the div where the label will be added. Ex : "#selectLabel".
 * @param {String} labelSize - size of col-xs-?? from 1 to 12. Default to 2 Ex : "4".
 * @param {String} test - Test Folder to Select.
 * @param {String} testCase - Test ID to Select.
 * @returns {null}
 */
function loadLabel(labelList, mySystem, myLabelDiv, labelSize, test, testCase) {

    if (isEmpty(labelSize)) {
        labelSize = "2";
    }
    var labelDiv = myLabelDiv;
    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.get("ReadLabel?system=" + targetSystem + "&withHierarchy=true&isSelectable=Y&testSelect=" + encodeURI(test) + "&testCaseSelect=" + encodeURI(testCase), "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        //DRAW LABEL LIST
        if (messageType === "success") {

            //DRAW LABEL TREE

            $('#selectLabelS').treeview({data: data.labelHierarchy.stickers, enableLinks: false, showTags: true, multiSelect: true});
            $('#selectLabelB').treeview({data: data.labelHierarchy.batteries, enableLinks: false, showTags: true, multiSelect: true});
            $('#selectLabelR').treeview({data: data.labelHierarchy.requirements, enableLinks: false, showTags: true, multiSelect: true});

            $('#selectLabelS').treeview('expandAll', {levels: 20, silent: true});
            $('#selectLabelB').treeview('expandAll', {levels: 20, silent: true});
            $('#selectLabelR').treeview('expandAll', {levels: 20, silent: true});

//            $(labelDiv + "S").empty();
//            $(labelDiv + "R").empty();
//            $(labelDiv + "B").empty();
//            var index;
//            for (index = 0; index < data.contentTable.length; index++) {
//                //the character " needs a special encoding in order to avoid breaking the string that creates the html element
//                var l = data.contentTable[index];
//                var labelTag = '<div style="float:left" align="center"><input name="labelid" id="labelId' + l.id + '" value="' + l.id + '" type="checkbox">\n\
//                <span class="label label-primary" style="cursor:pointer;background-color:' + l.color + '">' + l.label + '</span></div> ';
//                var option = $('<div style="float:left; height:60px" name="itemLabelDiv" id="itemLabelId' + l.id + '" class="col-xs-4 col-sm-2 list-group-item list-label"></div>')
//                        .attr("value", l.label).html(labelTag);
//                var a = "S";
//                if (l.type === "REQUIREMENT") {
//                    a = "R";
//                } else if (l.type === "BATTERY") {
//                    a = "B";
//                }
//                if (l.system === targetSystem) {
//                    $(labelDiv + a).prepend(option);
//                } else {
//                    $(labelDiv + a).append(option);
//                }
//            }
        } else {
            showMessageMainPage(messageType, data.message, true);
        }
        // Put the selected testcaselabel at the top and check them. 
//        if (!(isEmpty(labelList))) {
//            var index;
//            for (index = 0; index < labelList.length; index++) {
//                var l = labelList[index].label;
//                //For each testcaselabel, put at the top of the list and check them
//                var element = $("#itemLabelId" + l.id);

//                $('#selectLabelSb').treeview('selectNode', {levels: 20, silent: true, id : l.id});
//                $('#selectLabelBb').treeview('selectNode', l.id);
//                $('#selectLabelRb').treeview('selectNode', l.id);
//                tS.selectNode(l.id);

//                element.remove();
//                var a = "S";
//                if (l.type === "REQUIREMENT") {
//                    a = "R";
//                } else if (l.type === "BATTERY") {
//                    a = "B";
//                }
//                $(labelDiv + a).prepend(element);
//                $("#labelId" + l.id).prop("checked", true);
//            }
//        }
        //ADD CLICK EVENT ON LABEL
//        $(labelDiv + "S").find('span').click(function () {
//            var status = $(this).parent().find("input").prop('checked');
//            $(this).parent().find("input").prop('checked', !status);
//        });
    }).fail(handleErrorAjaxAfterTimeout);
}

function appendApplicationList(defautValue, mySystem) {

    $("[name=application]").empty();

    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.getJSON("ReadApplication", "q=1" + getUser().systemQuery);
    $.when(jqxhr).then(function (data) {
        var applicationList = $("[name=application]");

        for (var index = 0; index < data.contentTable.length; index++) {
            if (data.contentTable[index].system === targetSystem) {
                applicationList.prepend($('<option></option>').addClass('bold-option').text(data.contentTable[index].application).val(data.contentTable[index].application));
            } else {
                applicationList.append($('<option></option>').text(data.contentTable[index].application).val(data.contentTable[index].application));
            }
        }
        $("#application").val(defautValue);
    });
}

function appendTestList(defautValue) {
    $('#editTestCaseModal [name="test"]').empty();
    $('#editTestCaseModal [name="test"]').select2(getComboConfigTest());

//    var user = getUser();
//    $("#editTestCaseModal [name=test]").empty();
//
//    var jqxhr = $.getJSON("ReadTest", "");
//    $.when(jqxhr).then(function (data) {
//        var testList = $("[name=test]");
//
//        for (var index = 0; index < data.contentTable.length; index++) {
//            testList.append($('<option></option>').text(data.contentTable[index].test).val(data.contentTable[index].test));
//        }
//        testList.val(defautValue);
//
//    });

// Set Select2 Value.
    var myoption = $('<option></option>').text(defautValue).val(defautValue);
    $("#editTestCaseModal [name=test]").append(myoption).trigger('change'); // append the option and update Select2

}
