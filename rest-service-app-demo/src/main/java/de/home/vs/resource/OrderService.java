package de.home.vs.resource;

import de.home.vs.model.DataSource;
import de.home.vs.model.order.Order;
import java.util.Optional;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/orders")
public class OrderService {
    private final DataSource dataSource = DataSource.getInstance();
    private final OrderSerializer orderSerializer = new OrderSerializer("/orders/");
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders(){
        try {
            return Response
                    .ok()
                    .entity(orderSerializer.serializeOrders(dataSource.getOrders(), orderSerializer::serializeShort))
                    .build();
        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderById(@PathParam("id") int id){
        try {
            Optional<Order> order = dataSource.findOrderById(id);
            if (!order.isPresent()){
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(String.format("Order with id %d not found", id))
                        .build();
            }
            return Response
                    .ok()
                    .entity(orderSerializer.serializeFull(order.get()))
                    .build();
        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postOrder(JsonObject requestedOrder) {
        try {
            Order newOrder = createNewOrder(requestedOrder);
            dataSource.addOrder(newOrder);
            return Response.ok()
                    .entity(String.format("Order with id %s created successfully", newOrder.getId()))
                    .build();
        }catch (Exception e){
            return Response.serverError()
                    .entity(e.getMessage())
                    .build();
        }
    }


    private Order createNewOrder(JsonObject requestedOrder){
        return orderSerializer.deserializeWithNewId(requestedOrder, getNextOrderId());
    }

    private int getNextOrderId(){
        return dataSource.getOrders().size() + 1;
    }
}