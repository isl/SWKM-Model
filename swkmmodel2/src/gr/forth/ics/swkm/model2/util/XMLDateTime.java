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
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Calendar;

/**
 *
 * @author egiannak
 */
   /**
   * Describe an XML schema DateTime.
   * <p>The format is defined by W3C XML Schema Recommendation and ISO8601
   * i.e <tt>(-)CCYY-MM-DD'T'HH:MM:SS(.SSSSS)(Z|(+|-)hh:mm)</tt>
   */

public class XMLDateTime extends DateTimeBase{
/** Complaint string. */
    private static final String BAD_DATE = "Bad DateTime format: ";

    public XMLDateTime() {
    // Nothing for the default
    }

          /**
           * Constructs a XML Schema DateTime instance given all the values of the
           * different date and time (but not time zone) fields.
          * <p>
          * By default a DateTime is not UTC, and is local. To set a timezone, you
          * need to separately call {@link #setZone(short, short)}.
           *
           * @param values
           *            an array of shorts that represent the different fields of
           *            Time.
           * @see #setValues
          */
          public XMLDateTime(short[] values) {
              setValues(values);
          }

          /**
   61        * Creates a new XML Schema DateTime instance from a long that represents a
   62        * Date.  No time zone information is set.
   63        * <p>
   64        * By default a DateTime is not UTC, and is local. To set a timezone, you
   65        * need to separately call {@link #setZone(short, short)}.
   66        *
   67        * @param dateAsLong
   68        *            java.util.Date represented as a long.
   69        */
          public XMLDateTime(long dateAsLong) {
              this(new java.util.Date(dateAsLong));
          }

          /**
   75        * Creates a new XML Schema DateTime instance from a java.util.Date. No time
   76        * zone information is set.
   77        * <p>
   78        * By default a DateTime is not UTC, and is local. To set a timezone, you
   79        * need to separately call {@link #setZone(short, short)}.
   80        *
   81        * @param dateRef
   82        *            a java.util.Date to convert.
   83        */
          public XMLDateTime(java.util.Date dateRef) {
              GregorianCalendar tempCalendar = new GregorianCalendar();
              tempCalendar.setTime(dateRef);

              setCentury((short) (tempCalendar.get(Calendar.YEAR) / 100));
              setYear((short) (tempCalendar.get(Calendar.YEAR) % 100));
              // In GregorianCalendar, 0 <= Month <= 11; January == 0
              setMonth((short) (tempCalendar.get(Calendar.MONTH) + 1));
              setDay((short) tempCalendar.get(Calendar.DAY_OF_MONTH));

              setHour((short) tempCalendar.get(Calendar.HOUR_OF_DAY));
              setMinute((short) tempCalendar.get(Calendar.MINUTE));
              setSecond((short) tempCalendar.get(Calendar.SECOND), (short) tempCalendar.get(Calendar.MILLISECOND));
          }

          /**
  100        * Constructs a DateTime from a String. The String is expected to be in W3C
  101        * Schema DateTime format.
  102        *
  103        * @param date
  104        *            the string representing the date
  105        * @throws java.text.ParseException
  106        *             if we are passed an illegal value
  107        */
         public XMLDateTime(String date) throws java.text.ParseException {
             parseDateTimeInternal(date, this);
         }

         /**
  113        * Sets all the fields to the values provided in an Array. The Array must
  114        * be at least eight entries long.  Extra entries are ignored.  The order of
  115        * entries in the array is as follows:
  116        * <ul>
  117        * <li>century</li>
  118        * <li>year</li>
  119        * <li>month</li>
  120        * <li>day</li>
  121        * <li>hour</li>
  122        * <li>minute</li>
  123        * <li>second</li>
  124        * <li>millisecond</li>
  125        * </ul>
  126        * If a Time Zone is to be specified, it has to be set separately by using
  127        * {@link DateTimeBase#setZone(short, short) setZone}.  A time zone
  128        * previously set will not be cleared.
  129        *
  130        * @param values
  131        *            An array of shorts containing the values for the DateTime
  132        */
          public void setValues(short[] values) {
              if (values.length != 8) {
                  throw new IllegalArgumentException("DateTime#setValues: Array length "
                                                     + values.length + " != 8");
              }
              this.setCentury(values[0]);
              this.setYear(values[1]);
              this.setMonth(values[2]);
              this.setDay(values[3]);
              this.setHour(values[4]);
              this.setMinute(values[5]);
              this.setSecond(values[6], values[7]);
          }

         /**
  148        * Returns an array of shorts with all the fields that describe this
  149        * DateTime type. The order of entries in the array is as follows:
  150        * <ul>
  151        * <li>century</li>
  152        * <li>year</li>
  153        * <li>month</li>
  154        * <li>day</li>
  155        * <li>hour</li>
  156        * <li>minute</li>
  157        * <li>second</li>
  158        * <li>millisecond</li>
  159        * </ul>
  160        * Note:the time zone is not included.
  161        *
  162        * @return an array of short with all the fields that describe this Date
  163        *         type.
  164        */
         public short[] getValues() {
             short[] result = new short[8];
             result[0] = this.getCentury();
             result[1] = this.getYear();
             result[2] = this.getMonth();
             result[3] = this.getDay();
             result[4] = this.getHour();
             result[5] = this.getMinute();
             result[6] = this.getSeconds();
             result[7] = this.getMilli();
             return result;
         } //getValues

         /**
  179        * Converts this DateTime into a local java.util.Date.
  180        * @return a local java.util.Date representing this DateTime.
  181        */
         public java.util.Date toDate() {
             Calendar calendar = new GregorianCalendar(getCentury()*100+getYear(), getMonth()-1, getDay(), getHour(), getMinute(), getSeconds());
             calendar.set(Calendar.MILLISECOND, getMilli());
             setDateFormatTimeZone(calendar);
             return calendar.getTime();
         } //toDate()

         /**
  190        * Converts this DateTime into a long value representing a java.util.Date.
  191        * @return This DateTime instance as a long value representing a java.util.Date.
  192        */
         public long toLong() {
             return toDate().getTime();
         }

         /**
  198        * Converts this DateTime to a string. The format is defined by W3C XML
  199        * Schema recommendation and ISO8601: (+|-)CCYY-MM-DDTHH:MM:SS.SSS(+/-)HH:SS
  200        *
  201        * @return a string representing this Date
  202        */
          public String toString() {
             StringBuffer result = new StringBuffer();

             appendDateString(result);
             result.append('T');
             appendTimeString(result);
             appendTimeZoneString(result);

             return result.toString();
         } //toString

         /**
  215        * Parses a String into a new DateTime instance.
  216        *
  217        * @param str
  218        *            the string to parse
  219        * @return a new DateTime instance with the value of the parsed string.
  220        * @throws ParseException
  221        *             If the string to parse does not follow the right format
  222        */
         public static XMLDateTime parse(String str) throws ParseException {
             return parseDateTime(str);
         }

         /**
  228        * Parses a String into a new DateTime instance.
  229        *
  230        * @param str
  231        *            the string to parse
  232        * @return a new DateTime instance with the value of the parsed string.
  233        * @throws ParseException
  234        *             If the string to parse does not follow the right format
  235        */
         public static XMLDateTime parseDateTime(String str) throws ParseException {
             return parseDateTimeInternal(str, new XMLDateTime());
          }

         /**
  241        * Parses a String into the provided DateTime instance (or a new DateTime
  242        * instance if the one provided is null) and assigns that value to the
  243        * DateTime instance given.
  244        *
  245        * @param str
  246        *            the string to parse
  247        * @param result
  248        *            the DateTime instance to assign to the parsed value of the
  249        *            String.  If null is passed, a new DateTime instance is created
  250        *            to be returned.
  251        * @return the DateTime instance with the value of the parsed string.
  252        * @throws ParseException
  253        *             If the string to parse does not follow the right format
  254        */
         private static XMLDateTime parseDateTimeInternal(String str, XMLDateTime result) throws ParseException {
             if (str == null) {
                 throw new IllegalArgumentException("The string to be parsed must not be null.");
             }

             if (result == null) {
                 result = new XMLDateTime();
             }

             char[] chars = str.toCharArray();

             if (chars.length < 19) {
                 throw new ParseException(BAD_DATE + str + "\nDateTime is not long enough", 0);
             }

             int idx = 0;
             idx = parseYear(str, result, chars, idx, BAD_DATE);
             idx = parseMonth(str, result, chars, idx, BAD_DATE);
             idx = parseDay(str, result, chars, idx, BAD_DATE);

             if (chars[idx] != 'T') {
                 throw new ParseException(BAD_DATE + str + "\n 'T' " + DateTimeBase.WRONGLY_PLACED, idx);
             }

             idx++;

             idx = parseTime(str, result, chars, idx, BAD_DATE);
             parseTimeZone(str, result, chars, idx, BAD_DATE);

             return result;
         } //parse

  }
