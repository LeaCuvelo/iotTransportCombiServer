package taller2.combilocationiot.com.iottransportcombiserver.API;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IotRepositoryService {
    @POST("writesample")
    Call<String> writeSample(@Query("latitude") String latitude,
                             @Query("longitude") String longitude,
                             @Query("datetime") String datetime );
}
