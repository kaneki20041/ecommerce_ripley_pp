import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJackson {
    public static class SmartCartConfigRequest {
        private boolean isActive;
        private int upsellDiscountPercentage;

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public int getUpsellDiscountPercentage() { return upsellDiscountPercentage; }
        public void setUpsellDiscountPercentage(int upsellDiscountPercentage) { this.upsellDiscountPercentage = upsellDiscountPercentage; }
    }

    public static void main(String[] args) throws Exception {
        String json = "{\"active\":true, \"upsellDiscountPercentage\":25}";
        ObjectMapper mapper = new ObjectMapper();
        SmartCartConfigRequest req = mapper.readValue(json, SmartCartConfigRequest.class);
        System.out.println("upsell: " + req.getUpsellDiscountPercentage());
        System.out.println("active: " + req.isActive());
    }
}
