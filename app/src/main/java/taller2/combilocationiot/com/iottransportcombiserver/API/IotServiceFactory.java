package taller2.combilocationiot.com.iottransportcombiserver.API;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import taller2.combilocationiot.com.iottransportcombiserver.Constants;

public class IotServiceFactory {

    private static Retrofit retrofit = null;


    public static IotRepositoryService getApiService() {
        if(retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(IotRepositoryService.class);
    }
}
