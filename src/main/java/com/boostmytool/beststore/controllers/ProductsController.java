package com.boostmytool.beststore.controllers;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.services.ProductsRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"", "/"})
    public String showProductList(Model model){

        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("Products", products);

        return "products/index";

    }

    @GetMapping("/create")
    public String showCreateProductForm(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);

        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){

        if(productDto.getImageFile().isEmpty()){

        }

        if(result.hasErrors()){
            return "products/CreateProduct";
        }

        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){

                Files.createDirectory(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {

                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }

        }catch(Exception e){

            System.out.println("Exception: " + e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCreateAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);
            
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id){

        try{

            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);

        }catch(Exception e){

            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result){

        try{

            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if(result.hasErrors()){
                return "products/EditProduct";
            }

            if(!productDto.getImageFile().isEmpty()){

                String uploadDir = "public/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try{

                    Files.delete(oldImagePath);
                }catch(Exception e){

                    System.out.println("Exception: " + e.getMessage());
                }

                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){

                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }catch(Exception e){

                    System.out.println("Exception: " + e.getMessage());
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());

            repo.save(product);

        }catch(Exception e){

            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(Model model, @RequestParam int id){

        try{

            Product product = repo.findById(id).get();
            
            Path imagePath = Paths.get("public/" + product.getImageFileName());

            try{

                Files.delete(imagePath);
            }catch(Exception e){

                System.out.println("Exception: " + e.getMessage());
            }
            
            repo.delete(product);

        }catch(Exception e){

            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/products";
    }
    

}
