import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ReportGenerator {
    static class TaskRunnable implements Runnable {
        private final String path;
        private double totalCost;
        private int totalAmount;
        private int totalDiscountSum;
        private int totalLines;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;
        private int mostExpensiveAmount;

        public TaskRunnable(String path) {
            this.path = path;
            this.totalCost = 0;
            this.totalAmount = 0;
            this.totalDiscountSum = 0;
            this.totalLines = 0;
            this.highestCostAfterDiscount = 0;
            this.mostExpensiveProduct = null;
            this.mostExpensiveAmount = 0;
        }

        @Override
        public void run() {
            try {
                List<String> lines = Files.readAllLines(Paths.get(path));
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length != 3) continue;

                    int productId = Integer.parseInt(parts[0].trim());
                    int amount = Integer.parseInt(parts[1].trim());
                    int discount = Integer.parseInt(parts[2].trim());

                    Product product = findProductById(productId);
                    if (product == null) continue;

                    double originalCost = product.getPrice() * amount;
                    double discountedCost = originalCost - discount;

                    totalCost += discountedCost;
                    totalAmount += amount;
                    totalDiscountSum += discount;
                    totalLines++;

                    if (discountedCost > highestCostAfterDiscount) {
                        highestCostAfterDiscount = discountedCost;
                        mostExpensiveProduct = product;
                        mostExpensiveAmount = amount;
                    }
                }
            } catch (IOException e) {
            }
        }

        private Product findProductById(int productId) {
            for (Product product : productCatalog) {
                if (product != null && product.getProductID() == productId) {
                    return product;
                }
            }
            return null;
        }

        public void makeReport() {
            String filename = Paths.get(path).getFileName().toString();
            System.out.println(" Report for " + filename + " ");
            System.out.printf("Total cost: %.2f%n", totalCost);
            System.out.println("Total items bought: " + totalAmount);

            double averageDiscount = totalLines == 0 ? 0 : (double) totalDiscountSum / totalLines;
            System.out.printf("Average discount: %.2f%n", averageDiscount);

            if (mostExpensiveProduct != null) {
                System.out.println("Most expensive purchase after discount:");
                System.out.printf("- Product: %s (ID: %d)%n",
                        mostExpensiveProduct.getProductName(),
                        mostExpensiveProduct.getProductID());
                System.out.printf("- Amount: %d%n", mostExpensiveAmount);
                System.out.printf("- Total after discount: %.2f%n", highestCostAfterDiscount);
            }
            System.out.println();
        }
    }

    static class Product {
        private int productID;
        private String productName;
        private double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = productName;
            this.price = price;
        }

        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }

    private static final String[] ORDER_FILES = {
            "2021_order_details.txt",
            "2022_order_details.txt",
            "2023_order_details.txt",
            "2024_order_details.txt"
    };

    static Product[] productCatalog = new Product[10];

    public static void loadProducts() throws IOException {
        Path resourcePath = Paths.get("src", "main", "resources");
        Path productsPath = resourcePath.resolve("Products.txt");

        System.out.println("Loading products from: " + productsPath.toAbsolutePath());

        if (!Files.exists(productsPath)) {

        }

        List<String> lines = Files.readAllLines(productsPath);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length != 3) continue;

            int productId = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            double price = Double.parseDouble(parts[2].trim());

            for (int i = 0; i < productCatalog.length; i++) {
                if (productCatalog[i] == null || productCatalog[i].getProductID() == productId) {
                    productCatalog[i] = new Product(productId, name, price);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            loadProducts();
        } catch (IOException e) {
        }

        Thread[] threads = new Thread[ORDER_FILES.length];
        TaskRunnable[] tasks = new TaskRunnable[ORDER_FILES.length];


        for (int i = 0; i < ORDER_FILES.length; i++) {
            Path filePath = Paths.get("src", "main", "resources", ORDER_FILES[i]);
            tasks[i] = new TaskRunnable(filePath.toString());
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }


        for (Thread thread : threads) {
            thread.join();
        }


        for (TaskRunnable task : tasks) {
            task.makeReport();
        }
    }
}