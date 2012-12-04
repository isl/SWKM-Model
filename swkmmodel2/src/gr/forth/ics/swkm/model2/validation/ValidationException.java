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

import java.util.Collection;

/**
 * This exception is thrown to denote that an error has been found during validation.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ValidationException extends RuntimeException {
    /**
     * Creates a ValidationException with a message
     * 
     * @param message the message of the created exception
     */
    public ValidationException(String message) {
        super(message);
    }

    ValidationException(Collection<ValidationProblem> errors, Collection<ValidationProblem> warnings) {
        super(toString(errors, warnings));
    }
    
    private static String toString(Collection<ValidationProblem> errors, Collection<ValidationProblem> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("Errors (").append(errors.size()).append("): \n");
        int id = 0;
        for (ValidationProblem error : errors) {
            sb.append("[").append(++id).append("] ").append(error.getMessage())
                    .append(" (Error code: ").append(error.getErrorCode()).append(")\n");
        }
        id = 0;
        for (ValidationProblem warning : warnings) {
            sb.append("[").append(++id).append("] ").append(warning.getMessage())
                    .append(" (Error code: ").append(warning.getErrorCode()).append(")\n");
        }
        return sb.toString();
    }
}
