package taller2.combilocationiot.com.iottransportcombiserver.API;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IotRepositoryService {

    @POST("writesample")
    Call<String> writeSample(@Header("Content-Type") String content_type, @Body String body);
}

 //   Call<playlist> addToPlaylist(@Header("Content-Type") String content_type, @Body PlaylistParm parm);

