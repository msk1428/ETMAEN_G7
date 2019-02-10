package com.g7.mn.etmaen_g7.networking.api;

import com.g7.mn.etmaen_g7.model.AddFaceResponse;
import com.g7.mn.etmaen_g7.model.DetectFaceResponse;
import com.g7.mn.etmaen_g7.model.FindSimilar;
import com.g7.mn.etmaen_g7.model.FindSimilarResponse;
import com.g7.mn.etmaen_g7.networking.Routes;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.g7.mn.etmaen_g7.utlis.Constants.FACE_LIST_ID;

public interface Service {
//http methods   https://westus.api.cognitive.microsoft.com/face/v1.0/-----

    @POST(Routes.ADD_FACE + FACE_LIST_ID +  "/persistedFaces")
    Call<AddFaceResponse> addFace(@Query("userData") String userData, @Body RequestBody photo);

    @POST(Routes.DETECT_FACE)
    Call<List<DetectFaceResponse>> detectFace(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Body RequestBody photo);

    @POST(Routes.FIND_SIMILAR)
    Call<List<FindSimilarResponse>> fetchSimilar(@Body FindSimilar findSimilar) ;

    @DELETE(Routes.DETECT_FACE + FACE_LIST_ID + "{persistedFaceId}")
    Call<Void> deleteFace(@Path("persistedFaceId") String persistedFaceId);

}
