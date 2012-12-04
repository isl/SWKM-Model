/* 
 *  COPYRIGHT (c) 2008-2009 by Institute of Computer Science, 
 *  Foundation for Research and Technology - Hellas
 *  Contact: 
 *      POBox 1385, Heraklio Crete, GR-700 13 GREECE
 *      Tel:+30-2810-391632
 *      Fax: +30-2810-391638
 *      E-mail: isl@ics.forth.gr
 *      http://www.ics.forth.gr/isl
 *
 *   Authors  :  Dimitris Andreou, Nelly Vouzoukidou.
 *
 *   This file is part of SWKM model APIs (see also http://athena.ics.forth.gr:9090/SWKM/).
 *
 *    SWKM model APIs is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *   SWKM model APIs is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SWKM model APIs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   SWKM has been partially supported by EU project KP-Lab (IP IST-27490) kp-lab.org
 */


package gr.forth.ics.swkm.model2.validation;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * A validation handler that simply stores reported errors and warnings.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ProblemCollector implements ValidationHandler {
    private final List<ValidationProblem> errors = Lists.newArrayList();
    private final List<ValidationProblem> warnings = Lists.newArrayList();

    /**
     * Stores an error in this problem collector.
     * 
     * @param error the error to be stored
     */
    public void handleError(ValidationProblem error) {
        errors.add(error);
    }

    /**
     * Stores a warning in this problem collector.
     * 
     * @param warning the warning to be stored
     */
    public void handleWarning(ValidationProblem warning) {
        warnings.add(warning);
    }
    
    /**
     * Returns an unmodifiable list of all reported errors.
     * 
     * @return an unmodifiable list of all reported errors
     */
    public List<ValidationProblem> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * Returns an unmodifiable list of all reported warnings.
     * 
     * @return an unmodifiable list of all reported warnings
     */
    public List<ValidationProblem> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Checks that there is no error, or throws a {@code ValidationException} otherwise.
     * 
     * @throws ValidationException if there is any error
     */
    public void checkNoError() throws ValidationException {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors, warnings);
        }
    }

    /**
     * Checks that there is no error or warning, or throws a {@code ValidationException} otherwise.
     *
     * @throws ValidationException if there is any error or warning
     */
    public void checkNoErrorOrWarning() {
        if (!errors.isEmpty() || !warnings.isEmpty()) {
            throw new ValidationException(errors, warnings);
        }
    }
    
    /**
     * {@inheritDoc }
     * 
     * @return {@inheritDoc }
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!errors.isEmpty()) {
            sb.append("Errors: \n");
            for (ValidationProblem problem : errors) {
                sb.append("    ").append(problem).append("\n");
            }
        }
        if (!warnings.isEmpty()) {
            sb.append("Warnings: \n");
            for (ValidationProblem problem : errors) {
                sb.append("    ").append(problem).append("\n");
            }
        }
        if (errors.isEmpty() && warnings.isEmpty()) {
            sb.append("(No errors or warnings)");
        }
        return sb.toString();
    }
}
