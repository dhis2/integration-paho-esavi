/*
 * Copyright (c) 2004-2023, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.esavi.converters.v1;

import static org.springframework.util.StringUtils.hasText;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

import org.hisp.dhis.esavi.domain.Option;
import org.hisp.dhis.esavi.domain.OptionSet;
import org.hisp.dhis.esavi.domain.tracker.*;

@Data
public class EsaviContext
{
    private final TrackedEntity trackedEntity;

    private final Map<String, String> dataValues = new HashMap<>();

    private final Map<String, String> attributes = new HashMap<>();

    private final static Map<String, Map<String, String>> optionSets = new ConcurrentHashMap<>();

    public EsaviContext( TrackedEntity trackedEntity )
    {
        this.trackedEntity = trackedEntity;
        setup();
    }

    public TrackedEntity getTrackedEntity()
    {
        return trackedEntity;
    }

    public Enrollment getEnrollment()
    {
        if ( trackedEntity.getEnrollments().isEmpty() )
        {
            throw new RuntimeException( "No enrollments found." );
        }

        return trackedEntity.getEnrollments().get( 0 );
    }

    public String dataElement( String id )
    {
        if ( !hasDataElement( id ) )
        {
            throw new IllegalArgumentException( "Unknown dataElement: " + id );
        }

        return dataValues.get( id );
    }

    public String dataElement( String id, String defaultValue )
    {
        if ( hasDataElement( id ) )
        {
            return dataElement( id );
        }

        return defaultValue;
    }

    public boolean hasDataElement( String id )
    {
        return dataValues.containsKey( id ) && hasText( dataValues.get( id ) );
    }

    public String dataElementAsBoolean( String id )
    {
        String value = dataValues.get( id );

        if ( !hasText( value ) )
        {
            return "false";
        }

        return String.valueOf( "true".equals( value ) );
    }

    public boolean dataElementIsTrue( String id )
    {
        return "true".equals( dataElementAsBoolean( id ) );
    }

    public String attribute( String id )
    {
        if ( !hasAttribute( id ) )
        {
            throw new IllegalArgumentException( "Unknown attribute: " + id );
        }

        return attributes.get( id );
    }

    public String attribute( String id, String defaultValue )
    {
        if ( hasAttribute( id ) )
        {
            return attribute( id );
        }

        return defaultValue;
    }

    public boolean hasAttribute( String id )
    {
        return attributes.containsKey( id ) && hasText( attributes.get( id ) );
    }

    public String attributeAsBoolean( String id )
    {
        String value = attribute( id );

        if ( !hasText( value ) )
        {
            return "false";
        }

        return String.valueOf( "true".equals( value ) );
    }

    public boolean attributeIsTrue( String id )
    {
        return "true".equals( attributeAsBoolean( id ) );
    }

    public boolean hasOption( String optionSet, String code )
    {
        if ( optionSets.containsKey( optionSet ) )
        {
            return optionSets.get( optionSet ).containsKey( code );
        }

        return false;
    }

    public String option( String optionSet, String code )
    {
        if ( hasOption( optionSet, code ) )
        {
            return optionSets.get( optionSet ).get( code );
        }

        throw new IllegalArgumentException( "Unknown optionSet/code: " + optionSet + "/" + code );
    }

    public String option( String optionSet, String code, String defaultValue )
    {
        if ( hasOption( optionSet, code ) )
        {
            return optionSets.get( optionSet ).get( code );
        }

        return defaultValue;
    }

    public Map<String, Map<String, String>> optionSets()
    {
        return optionSets;
    }

    private void setup()
    {
        for ( TrackedEntityAttribute attribute : trackedEntity.getAttributes() )
        {
            attributes.put( attribute.getAttribute(), attribute.getValue() );
        }

        if ( trackedEntity.getEnrollments().isEmpty() )
        {
            return;
        }

        Enrollment enrollment = trackedEntity.getEnrollments().get( 0 );

        for ( Event event : enrollment.getEvents() )
        {
            for ( DataValue dataValue : event.getDataValues() )
            {
                dataValues.put( dataValue.getDataElement(), dataValue.getValue() );
            }
        }
    }

    public static void addOptionSet( OptionSet optionSet )
    {
        if ( optionSet == null || optionSets.containsKey( optionSet.getId() ) )
        {
            return;
        }

        optionSets.put( optionSet.getId(), new HashMap<>() );

        for ( Option option : optionSet.getOptions() )
        {
            optionSets.get( optionSet.getId() ).put( option.getCode(), option.getName() );
        }
    }
}
