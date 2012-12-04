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

import com.google.common.base.Preconditions;

/**
 * A validation problem, which can be a warning or an error.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 * @see Validator
 */
public class ValidationProblem {
    private final String message;
    private final ErrorCode errorCode;
    private final boolean isError;
    
    private ValidationProblem(String message, ErrorCode errorCode, boolean isError) {
        this.message = Preconditions.checkNotNull(message);
        this.errorCode = Preconditions.checkNotNull(errorCode);
        this.isError = isError;
    }
    
    /**
     * Creates a ValidationProblem denoting an error, with the specified message and error code.
     * 
     * @param message the message of the error
     * @param errorCode the code of the error
     * @return a ValidationProblem denoting an error, with the specified message and error code
     */
    static ValidationProblem error(String message, ErrorCode errorCode) {
        return new ValidationProblem(message, errorCode, true);
    }
    
    /**
     * Creates a ValidationProblem denoting an warning, with the specified message and error code.
     * 
     * @param message the message of the warning
     * @param errorCode the code of the warning
     * @return a ValidationProblem denoting an warning, with the specified message and error code
     */
    static ValidationProblem warning(String message, ErrorCode errorCode) {
        return new ValidationProblem(message, errorCode, false);
    }
    
    /**
     * Returns the message associated with this validation problem.
     * 
     * @return the message associated with this validation problem
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns the error code associated with this validation problem.
     * 
     * @return the error code associated with this validation problem.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Returns true if this validation problem is an error, false otherwise.
     * 
     * @return true if this validation problem is an error, false otherwise
     */
    public boolean isError() {
        return isError;
    }
    
    /**
     * Returns true if this validation problem is a warning, false otherwise.
     * 
     * @return true if this validation problem is a warning, false otherwise
     */
    public boolean isWarning() {
        return !isError;
    }

    void handledBy(ValidationHandler handler) {
        if (isError) {
            handler.handleError(this);
        } else {
            handler.handleWarning(this);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + errorCode + "] " + message;
    }
}
