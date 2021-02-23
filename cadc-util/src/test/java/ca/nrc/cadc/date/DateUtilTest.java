/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2016.                            (c) 2016.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.date;

import ca.nrc.cadc.util.Log4jInit;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author pdowler
 */
public class DateUtilTest 
{
    private static Logger log = Logger.getLogger(DateUtilTest.class);

    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.date", Level.INFO);
    }

    @Test
    public void template()
        throws Exception
    {
        try
        {
            
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testIVOAInvalidTZ()
        throws Exception
    {
        try
        {
            DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.LOCAL);
        }
        catch(IllegalArgumentException expected)
        {
            log.info("caught " + expected.getClass().getSimpleName() + " for invalid TZ");
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testIVOA()
        throws Exception
    {
        String ivoaDateStr = "2000-01-02T00:00:00.000";
        String[] flexOK = 
        {
            ivoaDateStr,
            "2000-01-02T00:00:00",
            "2000-01-02"
        };
        
        try
        {
            DateFormat df;
            
            df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, TimeZone.getTimeZone("UTC"));
            Assert.assertEquals(DateUtil.UTC, df.getTimeZone());
            
            df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);
            Assert.assertTrue(DateUtil.UTC == df.getTimeZone());
            
            Date expected = new Date();
            
            String str = df.format(expected);
            Date actual = df.parse(str);
            Assert.assertEquals(expected, actual);
            
            Date ivoaDate = df.parse(ivoaDateStr);
            Assert.assertNotNull(ivoaDate);
            
            for (String s : flexOK)
            {
                Date d = DateUtil.flexToDate(s, df);
                Assert.assertNotNull(d);
                Assert.assertEquals(ivoaDate, d);
                String s2 = df.format(d);
                Assert.assertEquals(ivoaDateStr, s2);
            }
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testIso8601Local()
        throws Exception
    {
        try
        {
            DateFormat df = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_LOCAL, DateUtil.LOCAL);
            
            DateFormat df2 = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_MSLOCAL, DateUtil.LOCAL);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testISO()
        throws Exception
    {
        try
        {
            Date expected = new Date();
            DateFormat df = DateUtil.getDateFormat(DateUtil.ISO_DATE_FORMAT, null);
            String str = df.format(expected);
            Date actual = df.parse(str);
            Assert.assertEquals(expected, actual);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testISO8601()
        throws Exception
    {
        try
        {
            Date expected = new Date();
            DateFormat df = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_MSZ, DateUtil.UTC);
            String str = df.format(expected);
            Date actual = df.parse(str);
            Assert.assertEquals(expected, actual);
            
            String iso8601ms = "2000-01-02T01:02:03.456Z";
            expected = df.parse(iso8601ms);
            Assert.assertNotNull(expected);
            str = df.format(expected);
            Assert.assertEquals(iso8601ms, str);
            
            expected = new Date(1000L * (System.currentTimeMillis()/1000L) ); // zero-millis
            df = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_Z, DateUtil.UTC);
            str = df.format(expected);
            actual = df.parse(str);
            Assert.assertEquals(expected, actual);
            
            String iso8601 = "2000-01-02T01:02:03Z";
            expected = df.parse(iso8601);
            Assert.assertNotNull(expected);
            str = df.format(expected);
            Assert.assertEquals(iso8601, str);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Ignore
    @Test
    // Broken and could not find any useage of HTTP_DATE_FORMAT
    public void testHttp_Date()
        throws Exception
    {
        try
        {
            DateFormat df = DateUtil.getDateFormat(DateUtil.HTTP_DATE_FORMAT, DateUtil.GMT);
            
            // https://tools.ietf.org/html/rfc7231#section-7.1.1.1
            String rfc7231 = "Sun, 06 Nov 1994 08:49:37 GMT";
            
            Date d = df.parse(rfc7231);
            Assert.assertNotNull(d);
            String actual = df.format(d);
            Assert.assertEquals(rfc7231, actual);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
}
