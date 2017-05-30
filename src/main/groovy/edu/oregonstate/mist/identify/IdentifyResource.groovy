package edu.oregonstate.mist.identify

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.identify.db.IdentifyDAO
import groovy.transform.TypeChecked
import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("identify")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class IdentifyResource extends Resource {
    private IdentifyDAO identifyDAO

    IdentifyResource(IdentifyDAO identifyDAO) {
        this.identifyDAO = identifyDAO
    }

    @Timed
    @GET
    Response identify(@QueryParam('osuID') String osuID,
                      @QueryParam('facilityCode') String facilityCode,
                      @QueryParam('cardID') String cardID) {
        if (!osuID && !facilityCode && !cardID) {
            return badRequest("No query parameters provided.").build()
        } else if (osuID && facilityCode && cardID) {
            return badRequest("Too many query parameters." +
                    " Use osuID, or a combination of facilityCode and cardID.").build()
        } else if (osuID && (cardID || facilityCode)) {
            return badRequest("osuID cannot be given with any other query parameter.").build()
        } else if ((facilityCode && !cardID) || (!facilityCode && cardID)) {
            return badRequest("facilityCode and cardID must be used together.").build()
        }

        def resultObject = new ResultObject()

        if (osuID && !facilityCode && !cardID) {
            resultObject.data = identifyDAO.getByOSUID(osuID)
        } else if (!osuID && facilityCode && cardID) {
            resultObject.data = identifyDAO.getByProxID(facilityCode + cardID)
        }

        ok(resultObject).build()
    }
}
