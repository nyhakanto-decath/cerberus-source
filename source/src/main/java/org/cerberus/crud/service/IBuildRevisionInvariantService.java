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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.BuildRevisionInvariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IBuildRevisionInvariantService {

    AnswerItem readByKey(String system, Integer level, Integer seq);

    AnswerItem readByKey(String system, Integer level, String versionName);

    /**
     *
     * @param system
     * @param level
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList readBySystemByCriteria(List<String> system, Integer level, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @param level
     * @return
     */
    AnswerList readBySystemLevel(List<String> system, Integer level);

    /**
     *
     * @param system
     * @return
     */
    AnswerList readBySystem(List<String> system);

    boolean exist(String system, Integer level, Integer seq);

    boolean exist(String system, Integer level, String versionName);

    Answer create(BuildRevisionInvariant buildRevisionInvariant);

    Answer delete(BuildRevisionInvariant buildRevisionInvariant);

    Answer update(String system, Integer level, Integer seq, BuildRevisionInvariant buildRevisionInvariant);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    BuildRevisionInvariant convert(AnswerItem<BuildRevisionInvariant> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<BuildRevisionInvariant> convert(AnswerList<BuildRevisionInvariant> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<List<String>> readDistinctValuesByCriteria(List<String> system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
