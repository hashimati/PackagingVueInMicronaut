package io.hashimati;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

@Controller
public class IndexController {

    @View("index")
    @Get("/")
    public HttpResponse<String> index()
    {
       return  HttpResponse.ok();
    }
}
