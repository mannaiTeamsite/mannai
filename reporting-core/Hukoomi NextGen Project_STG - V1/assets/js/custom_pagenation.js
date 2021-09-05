$(document).ready(function(){

    $('#pagination-here').bootpag({
       total: 10,          
       page: 1,            
       maxVisible: 5,     
       leaps: true,
       href: "#result-page-{{number}}",
    })
    
    //page click action
    $('#pagination-here').on("page", function(event, num){
    //show / hide content or pull via ajax etc
    $("#content").html("Page " + num); 
    
    
    });
 })