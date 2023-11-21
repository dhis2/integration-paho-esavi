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
package org.hisp.dhis.integration.esavi.converters.v1;

public enum EsaviOutcomeCode
{
    RECOVERED_OR_RESOLVED( "1", "Recuperado Completamente" ),
    RECOVERING_OR_RESOLVING( "2", "En recuperación" ),
    NOT_RECOVERED_OR_NOT_RESOLVED( "3", "No recuperado" ),
    RECOVERED_OR_RESOLVED_WITH_SEQUELAE( "4", "Recuperado con secuelas" ),
    DIED( "5", "Muerte" ),
    UNKNOWN( "0", "Desconocido" );

    private final String system = "https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceCS";

    private final String code;

    private final String display;

    EsaviOutcomeCode( String code, String display )
    {
        this.code = code;
        this.display = display;
    }

    public String getSystem()
    {
        return system;
    }

    public String getCode()
    {
        return code;
    }

    public String getDisplay()
    {
        return display;
    }
}
