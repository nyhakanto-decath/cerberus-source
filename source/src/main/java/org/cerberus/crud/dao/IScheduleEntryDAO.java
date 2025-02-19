/* Cerberus Copyright (C) 2013 - 2017 cerberustesting
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.dao;

import java.sql.Timestamp;
import java.util.List;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author cdelage
 */
public interface IScheduleEntryDAO {

    /**
     *
     * @param id
     * @return
     */
    public AnswerItem<ScheduleEntry> readByKey(long id);

    /**
     *
     * @param scheduler
     * @return
     */
    public AnswerItem<Integer> create(ScheduleEntry scheduler);
    
    /**
     *
     * @return
     */
    public AnswerItem<List> readAllActive();
    
    /**
     *
     * @param scheduleEntryObject
     * @return
     */
    public Answer update(ScheduleEntry scheduleEntryObject);
    
    /**
     *
     * @param object
     * @return
     */
    public Answer delete(ScheduleEntry object);
    
    /**
     *
     * @param name
     * @return
     */
    public AnswerItem<List> readByName(String name);
    
    /**
     *
     * @param schedulerId
     * @param lastExecution
     * @return
     */
    public Answer updateLastExecution(long schedulerId, Timestamp lastExecution);
}
