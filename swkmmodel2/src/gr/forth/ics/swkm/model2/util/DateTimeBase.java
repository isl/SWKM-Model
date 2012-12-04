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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.forth.ics.swkm.model2.util;

/**
 *
 * @author egiannak
 */
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

      /**
   60    * The base class for date/time XML Schema types.
   61    * <p>
   62    * The validation of the date/time fields is done in the set methods and follows
   63    * <a href="http://www.iso.ch/markete/8601.pdf">the ISO8601 Date and Time Format</a>.
   64    * <p>
   74    */
      public abstract class DateTimeBase implements java.io.Serializable, Cloneable {
          /** Public constant referring to an indeterminate Date/Time comparison. */
          public static final int       INDETERMINATE   = -1;
          /** Public constant referring to a Date/Time comparison result of "less than". */
          public static final int       LESS_THAN       = 0;
          /** Public constant referring to a Date/Time comparison result of "equals". */
          public static final int       EQUALS          = 1;
          /** Public constant referring to a Date/Time comparison result of "greater than". */
          public static final int       GREATER_THAN    = 2;

          /** When comparing a date/time with a time zone to one without, the recommendation
   86        * says that 14 hours is the time zone offset to use for comparison. */
          protected static final int    MAX_TIME_ZONE_COMPARISON_OFFSET = 14;

          /** Convenience String for complaints. */
          protected static final String WRONGLY_PLACED  = " is wrongly placed.";

          /** true if this date/time type is negative. */
          private boolean               _isNegative     = false;
          /** The century field. */
          private short                 _century        = 0;
          /** The year field. */
          private short                 _year           = 0;
          /** The month field. */
          private short                 _month          = 0;
         /** The day field. */
         private short                 _day            = 0;
         /** the hour field. */
         private short                 _hour           = 0;
         /** the minute field. */
         private short                 _minute         = 0;
         /** the second field. */
         private short                 _second         = 0;
         /** the millsecond field. */
         private short                 _millsecond     = 0;
         /** true if the time zone is negative. */
         private boolean               _zoneNegative   = false;
         /** true if this date/time type has a time zone assigned. */
         private boolean               _UTC            = false;
         /** the time zone hour field. */
         private short                 _zoneHour       = 0;
         /** the time zone minute field. */
         private short                 _zoneMinute     = 0;

         //////////////////////////Abstract methods////////////////////////////////////

         /**
  122        * Returns a java.util.Date that represents the XML Schema Date datatype.
  123        * @return a java.util.Date that represents the XML Schema Date datatype.
  124        */
         public abstract Date toDate();

         /**
  128        * Sets all the fields by reading the values in an array.
  129        * @param values an array of shorts with the values.
  130        */
         public abstract void setValues(short[] values);

         /**
  134        * returns an array of short with all the fields that describe a date/time
  135        * type.
  136        * @return an array of short with all the fields that describe a date/time
  137        *         type.
  138        */
         public abstract short[] getValues();

         ////////////////////////////////////////////////////////////////////////////

         /**
  144        * Returns true if the given year represents a leap year. A specific year is
  145        * a leap year if it is either evenly divisible by 400 OR evenly divisible
  146        * by 4 and not evenly divisible by 100.
  147        *
  148        * @param year
  149        *            the year to test where 0 < year <= 9999
          * @return true if the given year represents a leap year
          */
         public final boolean isLeap(int year) {
             return ((year % 4) == 0 && (year % 100) != 0) || (year % 400) == 0;
         }

         /**
  157        * Returns true if the given year represents a leap year. A specific year is
  158        * a leap year if it is either evenly divisible by 400 OR evenly divisible
  159        * by 4 and not evenly divisible by 100.
  160        *
  161        * @param year
  162        *            the year to test where 0 <= year <= 99
  163        * @param century
  164        *            the century to test where 0 <= century <= 99
  165        * @return true if the given year represents a leap year
  166        */
         private final boolean isLeap(short century, short year) {
             return isLeap(century * 100 + year);
         }

         //////////////////////////Setter methods////////////////////////////////////

         /**
  174        * Set the negative field to true.
  175        * @throws UnsupportedOperationException
  176        *             this exception is thrown when changing the value of the
  177        *             "century+year is negative" field is not allowed.
  178        */
         public void setNegative() throws UnsupportedOperationException {
             _isNegative = true;
         }

         /**
          * Set the century field. Note: year 0000 is not allowed.
  185        * @param century the value to set
  186        * @throws UnsupportedOperationException
  187        *             this exception is thrown when changing the value of the
  188        *             century field is not allowed
  189        */
         public void setCentury(short century) throws UnsupportedOperationException {
             String err = "";
             if (century < 0) {
                 err = "century " + century + " must not be negative.";
                 throw new IllegalArgumentException(err);
             } else if (_year == 0 && century == 0 && _century != 0) {
                 err = "century:  0000 is not an allowed year.";
                 throw new IllegalArgumentException(err);
             }

             _century = century;
         }

         /**
  204        * Sets the Year field. Note: year 0000 is not allowed.
          *
  206        * @param year
  207        *            the year to set
  208        * @throws UnsupportedOperationException
  209        *             in an overridden method in a derived class if that derived
  210        *             class does not support the year element.
  211        */
         public void setYear(short year) throws UnsupportedOperationException {
             String err = "";
             if (year < 0) {
                 err = "year " + year + " must not be negative.";
                 throw new IllegalArgumentException(err);
             } else if (year == -1) {
                 if (_century != -1) {
                     err = "year can not be omitted unless century is also omitted.";
                     throw new IllegalArgumentException(err);
                 }
             } else if (year == 0 && _century == 0) {
                 err = "year:  0000 is not an allowed year";
                 throw new IllegalArgumentException(err);
             } else if (year > 99) {
                 err = "year " + year + " is out of range:  0 <= year <= 99.";
                 throw new IllegalArgumentException(err);
             }

             _year = year;
         }

         /**
  234        * Sets the Month Field. Note 1 <= month <= 12.
  235        * @param month the value to set up
  236        * @throws UnsupportedOperationException
  237        *             in an overridden method in a derived class if that derived
  238        *             class does not support the month element.
  239        */
         public void setMonth(short month) throws UnsupportedOperationException {
             String err = "";
             if (month == -1) {
                 if (_century != -1) {
                      err = "month cannot be omitted unless the previous component is also omitted.\n"
                            + "only higher level components can be omitted.";
                      throw new IllegalArgumentException(err);
                 }
             } else if (month < 1 || month > 12) {
                 err = "month " + month + " is out of range:  1 <= month <= 12";
                 throw new IllegalArgumentException(err);
             }

             _month = month;
         }

         /**
  257        * Sets the Day Field. Note:  This field is validated before the assignment
  258        * is done.
  259        *
  260        * @param day
  261        *            the value to set up
  262        * @throws UnsupportedOperationException
  263        *             in an overridden method in a derived class if that derived
  264        *             class does not support the day element.
  265        */
         public void setDay(short day) throws UnsupportedOperationException {
             String err = "";
             if  (day == -1) {
                 if (_month != -1) {
                     err = "day cannot be omitted unless the previous component is also omitted.\n"
                           + "only higher level components can be omitted.";
                     throw new IllegalArgumentException(err);
                 }
             } else if (day < 1) {
                 err = "day " + day + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             }

             short maxDay = maxDayInMonthFor(_century, _year, _month);
             if (day > maxDay) {
                 if (_month != 2) {
                     err = "day " + day + " is out of range for month " + _month + ":  "
                           + "1 <= day <= " + maxDay;
                     throw new IllegalArgumentException(err);
                 } else if (isLeap(_century, _year)) {
                     err = "day " + day + " is out of range for February in a leap year:  "
                           + "1 <= day <= 29";
                     throw new IllegalArgumentException(err);
                 } else {
                     err = "day " + day + " is out of range for February in a non-leap year:  "
                           + "1 <= day <= 28";
                     throw new IllegalArgumentException(err);
                 }
             }

             _day = day;
         }

         /**
  300        * Sets the hour field for this date/time type.
  301        *
  302        * @param hour
  303        *            the hour to set
  304        * @throws UnsupportedOperationException
  305        *             this exception is thrown when changing the value of the hour
  306        *             field is not allowed
  307        */
         public void setHour(short hour) throws UnsupportedOperationException {
             if (hour > 23) {
                 String err = "hour " + hour + " must be strictly less than 24";
                 throw new IllegalArgumentException(err);
             } else if (hour < 0) {
                 String err = "hour " + hour + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             }

             _hour = hour;
         }

         /**
  321        * set the minute field for this date/time type.
  322        *
  323        * @param minute
  324        *            the minute to set.
  325        * @throws UnsupportedOperationException
  326        *             this exception is thrown when changing the value of the
  327        *             minute field is not allowed
  328        */
         public void setMinute(short minute) throws UnsupportedOperationException {
             if (minute > 59) {
                 String err = "minute " + minute + " must be strictly less than 60.";
                 throw new IllegalArgumentException(err);
             } else if (minute < 0) {
                 String err = "minute " + minute + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             }

             _minute = minute ;
         }

         /**
  342        * Sets the seconds field for this date/time type, including fractional
  343        * seconds.  (In this implementation, fractional seconds are limited
  344        * to milliseconds and are truncated at millseconds if more precision
  345        * is provided.)
  346        *
  347        * @param second
  348        *            the second to set
  349        * @param millsecond
  350        *            the millisecond to set
  351        * @throws UnsupportedOperationException
  352        *             this exception is thrown when changing the value of the
  353        *             second field is not allowed
  354        */
         public void setSecond(short second,short millsecond) throws UnsupportedOperationException {
             setSecond(second);
             setMilliSecond(millsecond);
         }

         /**
  361        * Sets the seconds field for this date/time type, not including the
  362        * fractional seconds.  Any fractional seconds previously set is unmodified.
  363        *
  364        * @param second
  365        *            the second to set
  366        * @throws UnsupportedOperationException
  367        *             this exception is thrown when changing the value of the
  368        *             second field is not allowed
  369        */
         public void setSecond(short second) throws UnsupportedOperationException {
             if (second > 60) {
                String err = "seconds " + second + " must be less than 60";
                throw new IllegalArgumentException(err);
             } else if (second < 0) {
                String err = "seconds "+second+" cannot be negative.";
                throw new IllegalArgumentException(err);
             }

             _second = second;
         }

         /**
  383        * Sets the millisecond field for this date/time type.
  384        *
  385        * @param millisecond
  386        *            the millisecond to set
  387        * @throws UnsupportedOperationException
  388        *             this exception is thrown when changing the value of the
  389        *             millisecond field is not allowed
  390        */
         public void setMilliSecond(short millisecond) throws UnsupportedOperationException {
             if (millisecond < 0) {
                 String err = "milliseconds " + millisecond + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             } else if (millisecond > 999) {
                 String err = "milliseconds " + millisecond + " is out of bounds: 0 <= milliseconds <= 999.";
                 throw new IllegalArgumentException(err);
             }

             _millsecond = millisecond;
         }

         /**
  404        * Sets the UTC field.
  405        */
         public void setUTC() {
             _UTC = true;
         }

         /**
  411        * Sets the time zone negative field to true.
  412        *
  413        * @param zoneNegative
  414        *            indicates whether or not the time zone is negative.
  415        * @throws UnsupportedOperationException
  416        *             this exception is thrown when changing the time zone fields
  417        *             is not allowed
  418        */
         public void setZoneNegative(boolean zoneNegative) {
             _zoneNegative = zoneNegative;
         }

         /**
  424        * Sets the time zone fields for this date/time type. A call to this method
  425        * means that the date/time type used is UTC.
  426        * <p>
  427        * For a negative time zone, you first assign the absolute value of the time
  428        * zone using this method and then you call
  429        * {@link #setZoneNegative(boolean)}.
  430        *
  431        * @param hour
  432        *            The time zone hour to set. Must be positive.
  433        * @param minute
  434        *            The time zone minute to set.
  435        * @throws UnsupportedOperationException
  436        *             this exception is thrown when changing the value of the time
  437        *             zone fields is not allowed
  438        */
         public void setZone(short hour, short minute) {
             setZoneHour(hour);
             setZoneMinute(minute);
         }

         /**
  445        * Sets the time zone hour field for this date/time type. A call to this
  446        * method means that the date/time type used is UTC.
  447        * <p>
  448        * For a negative time zone, you first assign the absolute value of the time
  449        * zone using this method and then you call
  450        * {@link #setZoneNegative(boolean)}.
  451        *
  452        * @param hour
  453        *            the time zone hour to set.  Must be positive.
  454        * @throws UnsupportedOperationException
  455        *             this exception is thrown when changing the value of the time
  456        *             zone fields is not allowed
  457        */
         public void setZoneHour(short hour) {
             if (hour > 23) {
                 String err = "time zone hour " + hour + " must be strictly less than 24";
                 throw new IllegalArgumentException(err);
             } else if (hour < 0) {
                 String err = "time zone hour " + hour + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             }

             _zoneHour = hour;

             // Any call to setZone means that you use the date/time you use is UTC
             setUTC();
         }

         /**
  474        * Sets the time zone minute field for this date/time type. A call to this
  475        * method means that the date/time type used is UTC.
  476        *
  477        * @param minute
  478        *            the time zone minute to set
  479        * @throws UnsupportedOperationException
  480        *             this exception is thrown when changing the value of the time
  481        *             zone fields is not allowed
  482        */
         public void setZoneMinute(short minute) {
             if (minute > 59) {
                 String err = "time zone minute " + minute + " must be strictly lower than 60";
                 throw new IllegalArgumentException(err);
             } else if (minute < 0) {
                 String err = "time zone minute " + minute + " cannot be negative.";
                 throw new IllegalArgumentException(err);
             }

             _zoneMinute = minute;

             // Any call to setZone means that you use the date/time you use is UTC
             setUTC();
         }

         ////////////////////////Getter methods//////////////////////////////////////

         public boolean isNegative() throws UnsupportedOperationException {
             return _isNegative;
         }

         public short getCentury() throws UnsupportedOperationException {
             return _century;
         }

         public short getYear() throws UnsupportedOperationException {
             return _year;
         }

         public short getMonth() throws UnsupportedOperationException {
             return _month;
         }

         public short getDay() throws UnsupportedOperationException {
             return _day;
         }

         public short getHour() throws UnsupportedOperationException {
             return _hour;
         }

         public short getMinute() throws UnsupportedOperationException {
             return _minute;
         }

         public short getSeconds() throws UnsupportedOperationException {
             return _second;
         }

         public short getMilli() throws UnsupportedOperationException {
             return _millsecond;
         }

         /**
  537        * Returns true if this date/time type is UTC, that is, has a time zone
  538        * assigned. A date/time type is UTC if a 'Z' appears at the end of the
  539        * lexical representation type or if it contains a time zone.
  540        *
  541        * @return true if this type has a time zone assigned, else false.
  542        */
         public boolean isUTC() {
             return _UTC;
         }

         public boolean isZoneNegative() {
             return _zoneNegative;
         }

         public short getZoneHour() {
             return _zoneHour;
         }

         public short getZoneMinute() {
             return _zoneMinute;
         }

         ////////////////////////Getter methods//////////////////////////////////////

         public boolean hasIsNegative() {
             return true;
         }

         public boolean hasCentury() {
             return true;
         }

         public boolean hasYear() {
             return true;
         }

         public boolean hasMonth() {
             return true;
         }

         public boolean hasDay() {
             return true;
         }

         public boolean hasHour() {
             return true;
         }

         public boolean hasMinute() {
             return true;
         }

         public boolean hasSeconds() {
             return true;
         }

         public boolean hasMilli() {
             return true;
         }

         ////////////////////////////////////////////////////////////////////////////////

         /**
  600        * Adds a Duration to this Date/Time type as defined in <a
  601        * href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes">
  602        * Adding Duration to dateTimes (W3C XML Schema, part 2 appendix E).</a>
  603        * This version uses the algorithm defined in the document from W3C. A later
  604        * version may optimize it.
  605        * <p>
  606        * The modified Date/Time instance will keep the same time zone it started
  607        * with, if any.
  608        * <p>
  609        * Don't use getter methods but use direct field access for dateTime in
  610        * order to have the behaviour defined in the Recommendation document.
  611        *
  612        * @param duration
  613        *            the duration to add
  614        */
         /*public void addDuration(Duration duration) {
             int temp  = 0;
             int carry = 0;
             int sign  = (duration.isNegative()) ? -1 : 1;

             // First add the month and year.

             // Months
             try {
                 temp = _month + sign * duration.getMonth();
                 carry = fQuotient(temp - 1, 12);
                 this.setMonth((short) (modulo(temp - 1, 12) + 1));
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }

             // Years
             try {
                 temp = _century * 100 + _year + sign * duration.getYear() + carry;
                 this.setCentury((short) (temp / 100));
                 this.setYear((short) (temp % 100));
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }

             // Next, pin the day-of-month so it is not outside the new month.

             int tempDay = _day;
             if (tempDay < 1) {
                 tempDay = 1;
             } else {
                 int maxDay = maxDayInMonthFor(_century, _year, _month);
                 if (_day > maxDay) {
                     tempDay = maxDay;
                 }
             }

             // Next, add the time components

             // Seconds
             try {
                 temp = _millsecond + sign * (int)duration.getMilli();
                 carry = fQuotient(temp, 1000);
                 this.setMilliSecond((short)modulo(temp, 1000));

                 temp = _second + sign * duration.getSeconds() + carry;
                 carry = fQuotient(temp, 60);
                 this.setSecond((short)modulo(temp , 60));
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }

             // Minutes
             try {
                 temp = _minute + sign * duration.getMinute() + carry;
                 carry = fQuotient(temp, 60);
                 this.setMinute((short)modulo(temp , 60));
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }

             // Hours
             try {
                 temp = _hour + sign*duration.getHour() + carry;
                 carry = fQuotient(temp, 24);
                 this.setHour((short)modulo(temp , 24));
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }

             // Finally, set the day-of-month, rolling the month & year as needed

             // Days
             try {
                 tempDay += sign * duration.getDay() + carry;

                 // Loop until the day-of-month is within bounds
                 while (true) {
                     short maxDay = maxDayInMonthFor(_century, _year, _month);
                     if (tempDay < 1) {
                         tempDay = (short) (tempDay + maxDayInMonthFor(_century, _year, _month - 1));
                         carry = -1;
                     } else if (tempDay > maxDay) {
                         tempDay = (short)(tempDay - maxDay);
                         carry = 1;
                     } else {
                         break;
                     }

                     try {
                         temp = _month + carry;
                         this.setMonth((short)(modulo(temp - 1, 12) + 1));
                         temp = this.getCentury() * 100 + this.getYear() + fQuotient(temp - 1, 12);
                         this.setCentury((short) (temp / 100));
                         this.setYear((short) (temp % 100));
                     } catch (UnsupportedOperationException e) {
                         // Ignore
                     }
                 }

                 this.setDay((short)tempDay);
             } catch (UnsupportedOperationException e) {
                 // Ignore
             }
         } // addDuration
     */
         ///////////////////////W3C XML SCHEMA Helpers///////////////////////////////

         /**
  724        * Helper function defined in W3C XML Schema Recommendation part 2.
  725        * @see <a href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes">
  726        * W3C XML Schema Recommendation part 2</a>
  727        */
          private int fQuotient(int a, int b) {
              return (int) Math.floor((float)a / (float)b);
          }

          /**
  733         * Helper function defined in W3C XML Schema Recommendation part 2.
  734         * @see <a href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes">
  735         * W3C XML Schema Recommendation part 2</a>
  736         */
          private int modulo(int a, int b) {
              return a - fQuotient(a,b) * b;
          }

          /**
  742        * Returns the maximum day in the given month of the given year.
  743        *
  744        * @param year
  745        * @param month
  746        * @return the maximum day in the given month of the given year.
  747        */
         private final short maxDayInMonthFor(short century, short year, int month) {
             if (month == 4 || month == 6 || month == 9 || month == 11) {
                 return 30;
             } else if (month == 2) {
                 return (short) ((isLeap(century,year)) ? 29 : 28);
             } else {
                 return 31;
             }
         }

         ////////////////////////////////////////////////////////////////////////////

         /**
  761        * Normalizes a date/time datatype as defined in W3C XML Schema
  762        * Recommendation document: if a timeZone is present but it is not Z then we
  763        * convert the date/time datatype to Z using the addition operation defined
  764        * in <a
  765        * href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes">
  766        * Adding Duration to dateTimes (W3C XML Schema, part 2 appendix E).</a>
  767        *
  768        * @see #addDuration
  769        */
         public void normalize() {
             if (!isUTC() || (_zoneHour == 0 && _zoneMinute == 0)) {
                 return;
             }

             /*Duration temp = new Duration();
             temp.setHour(_zoneHour);
             temp.setMinute(_zoneMinute);
             if (isZoneNegative()) {
                 temp.setNegative();
             }

             this.addDuration(temp); */

             //reset the zone
             this.setZone((short)0, (short)0);
             this.setZoneNegative(false);
         }

         /**
  790        * Compares two date/time data types. The algorithm of comparison is defined
  791        * in <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">W3C XML Schema
  792        * Recommendation (section 3.2.7.3)</a>
  793        * <p>
  794        * The returned value will be one of:
  795        * <ul>
  796        * <li>INDETERMINATE (-1): this ? dateTime</li>
  797        * <li>LESS_THAN (0): this < dateTime</li>
  798        * <li>EQUALS (1): this == dateTime</li>
  799        * <li>GREATER_THAN (2): this > dateTime</li>
  800        * </ul>
  801        * <p>
  802        * FIXME:  This code does not compare time zones properly for date/time types
  803        * that do not contain a time.
  804        *
  805        * @param dateTime
  806        *            the dateTime to compare with the current instance.
  807        * @return the status of the comparison.
  808        */
         public int compareTo(DateTimeBase dateTime) {
             if (dateTime == null) {
                 throw new IllegalArgumentException("a Date/Time datatype cannot be compared with a null value");
             }

             // Make copies of the date/times we compare so we DO NOT MODIFY THE CURRENT VALUES!
             DateTimeBase tempDate1;
             DateTimeBase tempDate2;

             try {
                 tempDate1 = clone(this);
                 if (tempDate1.isUTC()) {
                     tempDate1.normalize();
                 }

                 tempDate2 = clone(dateTime);
                 if (tempDate2.isUTC()) {
                     tempDate2.normalize();
                 }
     //        } catch (InstantiationException e) {
     //            // This is a Castor coding error if this occurs -- it should never occur
     //            throw new RuntimeException(e);
     //        } catch (IllegalAccessException e) {
     //            // This is a Castor coding error if this occurs -- it should never occur
     //            throw new RuntimeException(e);
             } catch (CloneNotSupportedException e) {
                 // This is a Castor coding error if this occurs -- it should never occur
                 throw new RuntimeException("Unexpected 'clone not supported' Exception");
             }

             // If both date/time types are in Z-form (or both not), we just compare the fields.
             if (tempDate1.isUTC() == tempDate2.isUTC()) {
                 return compareFields(tempDate1, tempDate2);
             }

             // If datetime1 has a time zone and datetime2 does not
             if (tempDate1.isUTC()) {
                 tempDate2.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate2.normalize();
                 int result = compareFields(tempDate1, tempDate2);
                 if (result == LESS_THAN) {
                     return result;
                 }

                 // Restore time from previous offsetting
                 tempDate2.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate2.setZoneNegative(true);
                 tempDate2.normalize();

                 tempDate2.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate2.setZoneNegative(true);
                 tempDate2.normalize();
                 result = compareFields(tempDate1, tempDate2);
                 if (result == GREATER_THAN) {
                     return result;
                 }
                 return INDETERMINATE;
             }

             // If datetime2 has a time zone and datetime1 does not
             if (tempDate2.isUTC()) {
                 tempDate1.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate1.normalize();
                 int result = compareFields(tempDate1, tempDate2);
                 if (result == GREATER_THAN) {
                     return result;
                 }

                 // Restore time from previous offsetting
                 tempDate1.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate1.setZoneNegative(true);
                 tempDate1.normalize();

                 tempDate1.setZone((short)MAX_TIME_ZONE_COMPARISON_OFFSET,(short)0);
                 tempDate1.setZoneNegative(true);
                 tempDate1.normalize();
                 result = compareFields(tempDate1, tempDate2);
                 if (result == LESS_THAN) {
                     return result;
                 }
                 return INDETERMINATE;
             }

             return INDETERMINATE;
         }

         /**
  896        * Copies a dateTime instance of some type -- all fields may or may not be
  897        * present. Always copy all fields, even if they are 0, and create a full
  898        * DateTime with all fields. This allows quick comparisons (no exceptions
  899        * thrown, which are expensive).
  900        */
         private DateTimeBase copyDateTimeInstance(DateTimeBase dateTime) {
             DateTimeBase newDateTime = new XMLDateTime();
             newDateTime._isNegative   = dateTime._isNegative;
             newDateTime._century      = dateTime._century;
             newDateTime._year         = dateTime._year;
             newDateTime._month        = dateTime._month;
             newDateTime._day          = dateTime._day;
             newDateTime._hour         = dateTime._hour;
             newDateTime._minute       = dateTime._minute;
             newDateTime._second       = dateTime._second;
             newDateTime._millsecond   = dateTime._millsecond;
             newDateTime._zoneNegative = dateTime._zoneNegative;
             newDateTime._UTC          = dateTime._UTC;
             newDateTime._zoneHour     = dateTime._zoneHour;
             newDateTime._zoneMinute   = dateTime._zoneMinute;
             return newDateTime;
         }

         public DateTimeBase clone(DateTimeBase dateTime) throws CloneNotSupportedException {
             DateTimeBase newDateTime = (DateTimeBase) super.clone();
     //        newDateTime = (DateTimeBase) dateTime.getClass().newInstance();
             newDateTime.setValues(dateTime.getValues());
             if (dateTime.hasIsNegative() && dateTime.isNegative()) {
                newDateTime.setNegative();
             }
             if (dateTime.isUTC()) {
                 newDateTime.setUTC();
                 newDateTime.setZone(dateTime.getZoneHour(), dateTime.getZoneMinute());
                 newDateTime.setZoneNegative(dateTime.isZoneNegative());
             }
             return newDateTime;
         }

         private static int compareFields(DateTimeBase date1, DateTimeBase date2) {
             short field1;
             short field2;

             if (date1.hasCentury() != date2.hasCentury()) {
                 return INDETERMINATE;
             }

             if (date1.hasCentury() && date2.hasCentury()) {
                 field1 = date1.getCentury();
                 field2 = date2.getCentury();
                 if (field1 < field2) {
                     return LESS_THAN;
                 } else if (field1 > field2) {
                     return GREATER_THAN;
                 }
             }

             if (date1.hasYear() != date2.hasYear()) {
                 return INDETERMINATE;
             }

             if (date1.hasYear() && date2.hasYear()) {
                 field1 = date1.getYear();
                 field2 = date2.getYear();
                 if (field1 < field2) {
                     return LESS_THAN;
                 } else if (field1 > field2) {
                     return GREATER_THAN;
                 }
             }

             if (date1.hasMonth() != date2.hasMonth()) {
                 return INDETERMINATE;
             }

             if (date1.hasMonth() && date2.hasMonth()) {
                 field1 = date1.getMonth();
                 field2 = date2.getMonth();
                 if (field1 < field2) {
                     return LESS_THAN;
                 } else if (field1 > field2) {
                     return GREATER_THAN;
                 }
             }

             if (date1.hasDay() != date2.hasDay()) {
                 return INDETERMINATE;
             }

             if (date1.hasDay() && date2.hasDay()) {
                 field1 = date1.getDay();
                 field2 = date2.getDay();
                 if (field1 < field2) {
                     return LESS_THAN;
                 } else if (field1 > field2) {
                     return GREATER_THAN;
                 }
             }

             if (date1.hasHour() != date2.hasHour()) {
                 return INDETERMINATE;
             }

             if (date1.hasHour() && date2.hasHour()) {
                 field1 = date1.getHour();
                field2 = date2.getHour();
                if (field1 < field2) {
                    return LESS_THAN;
                } else if (field1 > field2) {
                    return GREATER_THAN;
                }
            }

            if (date1.hasMinute() != date2.hasMinute()) {
                return INDETERMINATE;
            }

            if (date1.hasMinute() && date2.hasMinute()) {
                field1 = date1.getMinute();
                field2 = date2.getMinute();
                if (field1 < field2) {
                    return LESS_THAN;
                } else if (field1 > field2) {
                    return GREATER_THAN;
                }
            }

            if (date1.hasSeconds() != date2.hasSeconds()) {
                return INDETERMINATE;
            }

            if (date1.hasSeconds() && date2.hasSeconds()) {
                field1 = date1.getSeconds();
                field2 = date2.getSeconds();
                if (field1 < field2) {
                    return LESS_THAN;
                } else if (field1 > field2) {
                    return GREATER_THAN;
                }
            }

            if (date1.hasMilli() != date2.hasMilli()) {
                return INDETERMINATE;
            }

            if (date1.hasMilli() && date2.hasMilli()) {
                field1 = date1.getMilli();
                field2 = date2.getMilli();
                if (field1 < field2) {
                    return LESS_THAN;
                } else if (field1 > field2) {
                    return GREATER_THAN;
                }
            }

            return EQUALS;
        }

        /**
 1054        * {@inheritDoc}
 1055        * Overrides the java.lang.Object#hashcode method.
 1056        */
        public int hashCode() {
            return _year^_month^_day^_hour^_minute^_second^_millsecond^_zoneHour^_zoneMinute;
        }

        /**
 1062        * {@inheritDoc}
 1063        * Overrides the java.lang.Object#equals method.
 1064        * @see #equals(Object)
 1065        */
        public boolean equals(Object object) {
            // No need to check if we are comparing two instances of the same class.
            // (if the class is not the same then #equal will return false).
            if (object instanceof DateTimeBase) {
                return equal((DateTimeBase) object);
            }
            return false;
        }

        /**
 1076        * Returns true if the present instance of date/time type is equal to the
 1077        * parameter.
 1078        * <p>
 1079        * The equals relation is as defined in the W3C XML Schema Recommendation,
 1080        * part2.
 1081        *
 1082        * @param dateTime
 1083        *            the date/time type to compare with the present instance
 1084        * @return true if the present instance is equal to the parameter false if
 1085        *         not
 1086        */
        protected boolean equal(DateTimeBase dateTime) {
            return EQUALS == this.compareTo(dateTime);
        } //equals

        /**
 1092        * converts this Date/Time into a local java Calendar.
 1093        * @return a local calendar representing this Date or Time
 1094        */
        public Calendar toCalendar(){
            Calendar result = new GregorianCalendar();
            result.setTime(toDate());
            return result;
        } //toCalendar()

        ////////////// COMMON CODE USED BY EXTENDING CLASSES ///////////////////////

        protected static int parseYear(final String str, final DateTimeBase result, final char[] chars,
                                       final int index, final String complaint) throws ParseException {
            int idx = index;

            if (chars[idx] == '-') {
                idx++;
                result.setNegative();
            }

            if (str.length() < idx + 4
                || !Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])
                || !Character.isDigit(chars[idx + 2]) || !Character.isDigit(chars[idx + 3])) {
                throw new ParseException(complaint + str + "\nThe Year must be 4 digits long", idx);
            }

            short value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            short value2 = (short) ((chars[idx+2] - '0') * 10 + (chars[idx+3] - '0'));

            if (value1 == 0 && value2 == 0) {
                throw new ParseException(complaint + str + "\n'0000' is not allowed as a year.", idx);
            }

            result.setCentury(value1);
            result.setYear(value2);

            idx += 4;

            return idx;
        }

        protected static int parseMonth(final String str, final DateTimeBase result, final char[] chars,
                                        final int index, final String complaint) throws ParseException {
            int idx = index;

            if (chars[idx] != '-') {
                throw new ParseException(complaint + str + "\n '-' " + DateTimeBase.WRONGLY_PLACED, idx);
            }

            idx++;

            if (str.length() < idx + 2 || !Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])) {
                throw new ParseException(complaint + str + "\nThe Month must be 2 digits long", idx);
            }

            short value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            result.setMonth(value1);

            idx += 2;
            return idx;
        }

        protected static int parseDay(final String str, final DateTimeBase result, final char[] chars,
                                      final int index, final String complaint) throws ParseException {
            int idx = index;

            if (chars[idx] != '-') {
                throw new ParseException(complaint + str + "\n '-' " + DateTimeBase.WRONGLY_PLACED, idx);
            }

            idx++;

            if (str.length() < idx + 2 || !Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])) {
                throw new ParseException(complaint + str + "\nThe Day must be 2 digits long", idx);
            }

            short value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            result.setDay(value1);

            idx += 2;
            return idx;
        }

        protected static int parseTime(final String str, final DateTimeBase result, final char[] chars,
                                       final int index, final String complaint) throws ParseException {
            int idx = index;

            if (str.length() < idx + 8) {
                throw new ParseException(complaint + str + "\nA Time field must be at least 8 characters long", idx);
            }

            if (!Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])) {
                throw new ParseException(complaint + str + "\nThe Hour must be 2 digits long", idx);
            }

            short value1;

            value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            result.setHour(value1);

            idx += 2;

            // Minutes
            if (chars[idx] != ':') {
                throw new ParseException(complaint + str + "\n ':#1' " + DateTimeBase.WRONGLY_PLACED, idx);
            }

            idx++;

            if (!Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])) {
                throw new ParseException(complaint+str+"\nThe Minute must be 2 digits long", idx);
            }

            value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            result.setMinute(value1);

            idx += 2;

            // Seconds
            if (chars[idx] != ':') {
                throw new ParseException(complaint + str + "\n ':#2' " + DateTimeBase.WRONGLY_PLACED, idx);
            }

            idx++;

            if (!Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])) {
                throw new ParseException(complaint + str + "\nThe Second must be 2 digits long", idx);
            }

            value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
            result.setSecond(value1);

            idx += 2;

            if (idx < chars.length && chars[idx] == '.') {
                idx++;

                long decimalValue = 0;
                long powerOfTen   = 1;
                while (idx < chars.length && Character.isDigit(chars[idx])) {
                    decimalValue = decimalValue * 10 + (chars[idx] - '0');
                    powerOfTen *= 10;
                    idx++;
                }

                // Explicitly truncate to milliseconds if more digits were provided
                if (powerOfTen > 1000) {
                    decimalValue /= (powerOfTen / 1000);
                    powerOfTen = 1000;
                } else if (powerOfTen < 1000) {
                    decimalValue *= (1000 / powerOfTen);
                    powerOfTen = 1000;
                }
                result.setMilliSecond((short)decimalValue);
            }

            return idx;
        }

        protected static int parseTimeZone(final String str, final DateTimeBase result, final char[] chars,
                                           final int index, final String complaint) throws ParseException {
            // If we're at the end of the string, there's no time zone to parse
            if (index >= chars.length) {
                return index;
            }

            int idx = index;

            if (chars[idx] == 'Z') {
                result.setUTC();
                return ++idx;
            }

            if (chars[idx] == '+' || chars[idx] == '-') {
                if (chars[idx] == '-') {
                    result.setZoneNegative(true);
                }
                idx++;
                if (idx + 5 > chars.length || chars[idx + 2] != ':'
                    || !Character.isDigit(chars[idx]) || !Character.isDigit(chars[idx + 1])
                    || !Character.isDigit(chars[idx + 3]) || !Character.isDigit(chars[idx + 4])) {
                    throw new ParseException(complaint+str+"\nTimeZone must have the format (+/-)hh:mm", idx);
                }
                short value1 = (short) ((chars[idx] - '0') * 10 + (chars[idx+1] - '0'));
                short value2 = (short) ((chars[idx+3] - '0') * 10 + (chars[idx+4] - '0'));
                result.setZone(value1,value2);
                idx += 5;
            }

            return idx;
        }

        /**
 1285        * Sets the time zone in the provided DateFormat.
 1286        * @param df
 1287        */
        protected void setDateFormatTimeZone(DateFormat df) {
            // If no time zone, nothing to do
            if (! isUTC()) {
                return;
            }

            int offset = (this.getZoneMinute() + this.getZoneHour() * 60) * 60 * 1000;
            offset = isZoneNegative() ? -offset : offset;

            SimpleTimeZone timeZone = new SimpleTimeZone(0,"UTC");
            timeZone.setRawOffset(offset);
            timeZone.setID(TimeZone.getAvailableIDs(offset)[0]);
            df.setTimeZone(timeZone);
        }

        /**
 1304        * Sets the time zone in the provided Calendar.
 1305        * @param calendar
 1306        */
        protected void setDateFormatTimeZone(Calendar calendar) {
            // If no time zone, nothing to do
            if (! isUTC()) {
                return;
            }

            int offset = (this.getZoneMinute() + this.getZoneHour() * 60) * 60 * 1000;
            offset = isZoneNegative() ? -offset : offset;

            SimpleTimeZone timeZone = new SimpleTimeZone(0,"UTC");
            timeZone.setRawOffset(offset);
            String[] availableIDs = TimeZone.getAvailableIDs(offset);
            if (availableIDs != null && availableIDs.length > 0) {
                timeZone.setID(availableIDs[0]);
            }
            calendar.setTimeZone(timeZone);
        }

        protected void appendDateString(StringBuffer result) {
            if (isNegative()) {
                result.append('-');
            }

            if ((this.getCentury() / 10) == 0) {
                result.append(0);
            }
            result.append(this.getCentury());

            if ((this.getYear() / 10) == 0) {
                result.append(0);
            }
            result.append(this.getYear());

            result.append('-');
            if ((this.getMonth() / 10) == 0) {
                result.append(0);
            }
            result.append(this.getMonth());

            result.append('-');
            if ((this.getDay()/10) == 0) {
                result.append(0);
            }
            result.append(this.getDay());
        }

        protected void appendTimeString(StringBuffer result) {
            if ((this.getHour()/10) == 0) {
                result.append(0);
            }
            result.append(this.getHour());

            result.append(':');

            if ((this.getMinute() / 10) == 0) {
                result.append(0);
            }
            result.append(this.getMinute());

            result.append(':');

            if ((this.getSeconds() / 10) == 0) {
                result.append(0);
            }
            result.append(this.getSeconds());

            if (this.getMilli() != 0) {
                result.append('.');
                if (this.getMilli() < 100){
                    result.append('0');
                    if (this.getMilli() < 10){
                        result.append('0');
                    }
                }
                result.append(this.getMilli());
            }
        }

        protected void appendTimeZoneString(StringBuffer result) {
            if (!isUTC()) {
                return;
            }

            // By default we append a 'Z' to indicate UTC
            if (this.getZoneHour() == 0 && this.getZoneMinute() == 0) {
                result.append('Z');
                return;
            }

            if (isZoneNegative()) {
                result.append('-');
            } else {
                result.append('+');
            }

            if ((this.getZoneHour()/10) == 0) {
                result.append(0);
            }
            result.append(this.getZoneHour());

            result.append(':');
            if ((this.getZoneMinute()/10) == 0) {
                result.append(0);
            }
            result.append(this.getZoneMinute());
        }

    } //-- DateTimeBase

