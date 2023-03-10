package sd.ufpi.core.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sd.ufpi.core.exceptions.AttributeNotFoundException;
import sd.ufpi.core.rest.anotations.RequestMapping;
import sd.ufpi.core.rest.exceptions.ClassControllerAlreadyExists;
import sd.ufpi.core.rest.exceptions.ClassNotController;
import sd.ufpi.core.rest.exceptions.UnparsedValueForTargetType;
import sd.ufpi.core.rest.exceptions.ValueIsRequiredInAnotation;
import sd.ufpi.core.rest.types.Request;

public class Root extends RequestResolver implements HttpHandler {
    public Root(){
        super();
    }

    public void handle(HttpExchange exchange) throws IOException {
        HttpParse parse = new HttpParse();
        Request request = parse.parse(exchange);
        String resultado;
        try {
            resultado = (String)resolver(request);

            byte[] parsed = resultado.getBytes("UTF8");
            exchange.sendResponseHeaders(200, parsed.length);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseBody().write(parsed, 0, parsed.length);
            exchange.close();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | UnparsedValueForTargetType | ClassNotFoundException | ValueIsRequiredInAnotation e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void addController(Object controller) throws ClassNotController, ClassControllerAlreadyExists{

        Class<?> classe = controller.getClass();

        RequestMapping request = classe.getAnnotation(RequestMapping.class);
        String path = null;

        if(request == null){
            throw new ClassNotController("A classe não foi anotada como um controller");
        }
        
        if(request != null){
            path = request.path();
            String paths[] = path.split("/");
            if(paths.length == 1){
                if(!paths[0].equals("/") && !paths[0].equals("")){
                    path = paths[0];
                }
            }else if(paths.length > 1) {
                path = paths[1];
            }
        }

        if(path == null){
            throw new AttributeNotFoundException("Controller is not contain path");
        }
        this.addController(path, controller);
    }
}
