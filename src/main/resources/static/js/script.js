console.log("This is Script File")

const togglesidebar =() =>{
	
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none")
		$(".content").css("margin-left","0%")
		
	}
	else{
		$(".sidebar").css("display","block")
		$(".content").css("margin-left","20%")
	}
};

const search =()=>{
	console.log("searching...")
	
	let query=$("#search-input").val();
	
	
	let url= `http://localhost:8080/search/${query}`;
	
	fetch(url)
	.then((reResponse) => {
		
		return reResponse.json();
	})
	.then((data)=>{
		
		let text=`<div class='list-group'>`
		
		data.forEach((contact)=>{
			text +=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name} </a>`
		});
		
		
		text+=`</div>`
		
		$(".search-result").html(text);
		$(".search-result").show();
	})
	
	
	if(query==''){
		
		$(".search-result").hide();
		
	}
	
}

