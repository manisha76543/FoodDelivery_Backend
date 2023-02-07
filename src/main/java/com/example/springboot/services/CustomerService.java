package com.example.springboot.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pomato.mainPackage.model.*;
import com.pomato.mainPackage.model.CustomerSignupResponse;
import com.pomato.mainPackage.model.User;
import com.pomato.mainPackage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodOrderRepository foodOrderRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    RestaurantRepository restaurantRepository;
    @Autowired
    MenuRepository menuRepository;

    @Value("${pepper}")
    String pepper;

    public CustomerSignupResponse register(User user){

        User newUser = new User();
        User currentUser = userRepository.findByEmail(user.getEmail());
        CustomerSignupResponse customerSignupResponse = new CustomerSignupResponse();

        if(currentUser != null){
            customerSignupResponse.setMessage("Customer already exists.");
            customerSignupResponse.setStatus(false);
        }
        else{
            String salt = BCrypt.gensalt();
            String hashedPassword = BCrypt.hashpw(user.getPassword() + pepper, salt );
            user.setPassword(hashedPassword);
            user.setSalt(salt);
            user.setJwtToken(BCrypt.gensalt());
            //user = userRepository.save(user);
            newUser = userRepository.save(user);

            customerSignupResponse.setUserId(newUser.getUserId());
            customerSignupResponse.setName(newUser.getName());
            customerSignupResponse.setEmail(newUser.getEmail());
            customerSignupResponse.setRole(newUser.getRole());
            customerSignupResponse.setContactNumber(newUser.getContactNumber());
            customerSignupResponse.setJwtToken(newUser.getJwtToken());

            customerSignupResponse.setMessage("Signup successful");
            customerSignupResponse.setStatus(true);
        }
        return customerSignupResponse;
    }

    public PlaceOrderResponse placeOrder(String jwtToken, PlaceOrder placeOrder) throws JsonProcessingException {
        FoodOrders foodOrder=new FoodOrders();
        Payment payment=new Payment();
        User user=userRepository.findByUserId(placeOrder.getUserId());
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        if (user.getJwtToken().equals(jwtToken)==false){
            placeOrderResponse.setMessage("jwtToken invalid");
            placeOrderResponse.setStatus(false);
            return placeOrderResponse;
        }
        else {
            foodOrder.setUserId(placeOrder.getUserId());
            foodOrder.setAddress(placeOrder.getAddress());
            foodOrder.setRestaurantId(placeOrder.getRestaurantId());
            ObjectMapper objectMapper=new ObjectMapper();
            foodOrder.setListOfItems(objectMapper.writeValueAsString(placeOrder.getListOfItems()));
            foodOrder.setOrderStatus("Order Placed");
            String st = String.valueOf(Timestamp.from(Instant.now()));
            foodOrder.setTimeStamp(st);
            FoodOrders temp = foodOrderRepository.save(foodOrder);
            if (temp == null) {
                placeOrderResponse.setMessage("Order saving failed");
                placeOrderResponse.setStatus(false);
                return placeOrderResponse;
            }
            payment.setOrderId(foodOrder.getOrderId());
            payment.setTimeStamp(st);
            payment.setRestaurantId(placeOrder.getRestaurantId());
            payment.setPaymentMethod(placeOrder.getPaymentMethod());
            payment.setAmount(placeOrder.getAmount());
            payment.setPaymentStatus("Success");
            Payment tempp = paymentRepository.save(payment);
            if (tempp == null) {
                placeOrderResponse.setMessage("Order payment saving failed");
                placeOrderResponse.setStatus(false);
                return placeOrderResponse;
            }
            placeOrderResponse.setMessage("Order Placed and Payment Successful");
            placeOrderResponse.setStatus(true);
            placeOrderResponse.setFoodOrders(foodOrder);
            return placeOrderResponse;
        }
    }

    
    public GetRestaurantResponse getAllRestaurant(String token){
        GetRestaurantResponse getRestaurantResponse=new GetRestaurantResponse();
        User user=userRepository.findByJwtToken(token);
        if(user!=null){
            getRestaurantResponse.setAllRestaurant(restaurantRepository.getAllRestaurants());
            getRestaurantResponse.setMessage("Successfully executed");
            getRestaurantResponse.setStatus(true);
            return getRestaurantResponse;
        }
        else{
            getRestaurantResponse.setAllRestaurant(Collections.emptyList());

            getRestaurantResponse.setStatus(false);
            getRestaurantResponse.setMessage("jwtToken invalid");
            return getRestaurantResponse;
        }
    }
    public ViewMenuResponse viewRestaurantMenu(String jwtToken,int restaurantId){
        ViewMenuResponse viewMenuResponse=new ViewMenuResponse();
        User user=userRepository.findByJwtToken(jwtToken);
        if(user!=null){
            viewMenuResponse.setStatus(true);
            viewMenuResponse.setMessage("Successfully Executed");
            viewMenuResponse.setItems(menuRepository.findRestaurantMenu(restaurantId));
        }
        else{
            viewMenuResponse.setStatus(false);
            viewMenuResponse.setMessage("jwtToken invalid");
            viewMenuResponse.setItems(Collections.emptyList());
        }
        return viewMenuResponse;
    }
    public boolean checkout(String jwtToken, int userId) {
        User user = userRepository.findByUserId(userId);
        PlaceOrderResponse placeOrderResponse=new PlaceOrderResponse();
        if (!user.getJwtToken().equals(jwtToken)) {
            placeOrderResponse.setMessage("jwtToken invalid");
            placeOrderResponse.setStatus(false);
            return placeOrderResponse.isStatus();
        }
        placeOrderResponse.setMessage("Checkout");
        placeOrderResponse.setStatus(true);
        return placeOrderResponse.isStatus();
    }
    public ViewOrderCustomerResponse viewOrders(String jwtToken, int userId) {
        ViewOrderCustomerResponse viewOrderCustomerResponse = new ViewOrderCustomerResponse();
        User user = userRepository.findByUserId(userId);
        if (!user.getJwtToken().equals(jwtToken)) {
            viewOrderCustomerResponse.setMessage("jwtToken invalid");
            viewOrderCustomerResponse.setStatus(false);
            return viewOrderCustomerResponse;
        }
        List<FoodOrders> ordersList = foodOrderRepository.getAllByUserId(userId);
        viewOrderCustomerResponse.setMessage("Fetched Orders");
        viewOrderCustomerResponse.setStatus(true);
        viewOrderCustomerResponse.setFoodOrders(ordersList);
        return viewOrderCustomerResponse;
    }
}
