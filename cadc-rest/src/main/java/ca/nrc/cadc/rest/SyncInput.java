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

package ca.nrc.cadc.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;

import ca.nrc.cadc.util.CaseInsensitiveStringComparator;

/**
 *
 * @author pdowler
 */
public class SyncInput
{
    private static final Logger log = Logger.getLogger(SyncInput.class);

    protected HttpServletRequest request;
    protected BufferedReader reader;
    private Map<String, Object> content = new TreeMap<>(new CaseInsensitiveStringComparator());
    private Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());

    private InlineContentHandler inlineContentHandler;

    public SyncInput(HttpServletRequest request, InlineContentHandler handler)
    	throws IOException
    {
        this.request = request;
        this.inlineContentHandler = handler;
    }

    public BufferedReader getReader() throws IOException
    {
        if (reader == null)
        {
            log.debug("opening reader");
            reader = request.getReader();
        }
        return reader;
    }

    public boolean isOpen()
    {
        return (reader != null);
    }

    public String getProtocol()
    {
        return request.getScheme();
    }

    /**
     * Get a request header value.
     *
     * @param name
     * @return
     */
    public String getHeader(String name)
    {
        return request.getHeader(name);
    }

    /**
     * Get a request parameter value.
     *
     * @param name
     * @return
     */
    public String getParameter(String name)
    {
        List<String> vals = params.get(name);
        if (vals != null && !vals.isEmpty())
            return vals.get(0);
        return null;
    }

    public Object getContent(String name)
    {
    	return content.get(name);
    }

    /**
     * Get all request parameter values.
     *
     * @param name
     * @return
     */
    public List<String> getParameters(String name)
    {
        return params.get(name);
    }

    public void init() throws IOException
    {
        if (request.getMethod().equals("GET") ||
                request.getMethod().equals("HEAD") ||
                request.getMethod().equals("DELETE"))
        {
            Enumeration<String> names = request.getParameterNames();
            while (names.hasMoreElements())
            {
                String name = names.nextElement();
                processParameter(name, request.getParameterValues(name));
            }
        }
        else
        {
            String contentType = request.getContentType();
            if (contentType != null)
            {
                int i = contentType.indexOf(';');
                if (i > 0)
                    contentType = contentType.substring(0, i);
            }
            log.debug("Content-Type: " + contentType);
            if (contentType != null && contentType.equalsIgnoreCase(RestAction.URLENCODED))
            {
                Enumeration<String> names = request.getParameterNames();
                while (names.hasMoreElements())
                {
                    String name = names.nextElement();
                    processParameter(name, request.getParameterValues(name));
                }
            }
            else if (contentType != null && contentType.startsWith(RestAction.MULTIPART))
            {
            	try
            	{
	                ServletFileUpload upload = new ServletFileUpload();
	                FileItemIterator itemIterator = upload.getItemIterator(request);
	                processMultiPart(itemIterator);
            	}
            	catch(FileUploadException ex)
            	{
            		throw new IOException("Failed to process " + RestAction.MULTIPART, ex);
            	}
            }
            else
            {
                processStream(null, contentType, request.getInputStream());
            }
        }
    }

    private void processParameter(String name, String[] values)
    {
    	this.params.put(name, Arrays.asList(values));
    }

    private void processMultiPart(FileItemIterator itemIterator)
        throws FileUploadException, IOException
    {
        while (itemIterator.hasNext())
        {
            FileItemStream item = itemIterator.next();
            String name = item.getFieldName();
            InputStream stream = item.openStream();
            if (item.isFormField())
                processParameter(name, new String[] { Streams.asString(stream) });
            else
                processStream(name, item.getContentType(), stream);
        }
    }

    private void processStream(String name, String contentType, InputStream inputStream)
        throws IOException
    {
    	if (inlineContentHandler == null)
    	{
    		log.warn("request includes inline content and inlne content handler is null");
    		// TODO: Need to figure if we need to process the stream to completion
    		return;
    	}

        InlineContentHandler.Content c = inlineContentHandler.accept(name, contentType, inputStream);
        content.put(c.name, c.value);
    }
}
