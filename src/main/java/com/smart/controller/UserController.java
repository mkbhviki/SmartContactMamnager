package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.aspectj.bridge.Message;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addComonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("username"+"  "+username);
		//get the user using username(Email)
		User user = userRepository.getUserByUserName(username);
		System.out.println("User  "+user);
		
		model.addAttribute("user", user);
		
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title", "Home");
		return "normal/user_dashboard";
		
	}
   //open add form handler
	@GetMapping("/add-contact")
	public String oprnAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contacts");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
		
	}
	//processing add contact form
	@PostMapping("/process-contact")
	public String processcontact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file, Principal principal,HttpSession session) {
		
		try {
		
		String name = principal.getName();
		 User user = this.userRepository.getUserByUserName(name);
		 
		 //processing and uploding file..
		 if(file.isEmpty()) {
			 System.out.println("file in empty");
			 contact.setImage("contact.png");
		 }else {
			contact.setImage(file.getOriginalFilename());
			
			File savefile = new ClassPathResource("static/img").getFile();
		    Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		 
		 }
		 
		 contact.setUser(user);
		 
		 user.getContacts().add(contact);
		 
		 this.userRepository.save(user);
		 
		 
		 
		System.err.println("Data"+contact);
		
		System.out.println("Data Added Succesfully");
		
		//message success....
	   //session.setAttribute("message", new Message("Successfully Registered !!" , "alert-success"));
		session.setAttribute("message", new com.smart.helper.Message("Successfully Addaded !! Add more Contact  !!" , "success"));
		}catch (Exception e) {
			System.out.println("Error"+e.getMessage());
			session.setAttribute("message", new com.smart.helper.Message("Something went Wrong !!" , "danger"));
		}
		
		return "normal/add_contact_form";
		
	}
	//show contact handler
	//per page =5[n]
	//current page =0[page]
	@GetMapping("/show_contacts/{page}")
	public  String showContacts(@PathVariable("page")Integer page, Model model,Principal principal) {
		
		model.addAttribute("title", "View Contacts");
		/*
		 * String username = principal.getName(); User user =
		 * this.userRepository.getUserByUserName(username); List<Contact> contacts =
		 * user.getContacts();
		 */
		String username = principal.getName();
		
		User user = this.userRepository.getUserByUserName(username);
		
		
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> Contacts=this.contactRepository.findContactsByuser(user.getId(),pageable);
		
		model.addAttribute("contacts", Contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", Contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId")Integer cId ,Model model,Principal principal) {
		System.err.println(cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) { 
			model.addAttribute("title", contact.getName());
		
			model.addAttribute("contact",contact); 
		}   
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cId,Model model,Principal principal, HttpSession session) {
		
		Optional<Contact> conOptional = this.contactRepository.findById(cId);
		Contact contact = conOptional.get();
		 //contact.setUser(null);
		
	     User user = this.userRepository.getUserByUserName(principal.getName());
		 user.getContacts().remove(contact);
		 
		 this.userRepository.save(user);
		 
		
		//this.contactRepository.delete(contact);
		session.setAttribute("message", new com.smart.helper.Message("Contact deleted successfully..!" , "success"));

		return "redirect:/user/show_contacts/0";
	}
	//open update form handler
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid, Model model) {
		model.addAttribute("title", "Update Contact");
		
		 Contact contact = this.contactRepository.findById(cid).get();
		 
		 model.addAttribute("contact",contact);
	
	return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,Principal principal, @RequestParam("profileImage")MultipartFile file,Model model,HttpSession session) {
		
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image..
			if(!file.isEmpty())
			{
				
			//file work	
				//reweite
				
				//delete old photo
				File deletefile = new ClassPathResource("static/img").getFile();
			    File file2 = new File(deletefile, oldContactDetail.getImage());
				
			    file2.delete();
				
				
				//update new photo
				File savefile = new ClassPathResource("static/img").getFile();
			    Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new com.smart.helper.Message("Contact is Updated successfully..!" , "success"));
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.err.println(contact.getName()+contact.getcId());
		
		return"redirect:/user/"+contact.getcId()+"/contact";
	}
	//Your profile Handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		
		return"/normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings"; 
	}
	//change password ..handler
	@PostMapping("/change-password")
	public String changePassword( @RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		System.err.println("old password"+ oldPassword);
		System.err.println("new password"+ newPassword);
		
		String username = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new com.smart.helper.Message("Password Change successfully..!" , "success"));
			
		}
		else {
			
			session.setAttribute("message", new com.smart.helper.Message("Please Enter Correct Old Password..!" , "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
		
	}
	
	
}
