var App = function () {

    function handleIEFixes() {
        //fix html5 placeholder attribute for ie7 & ie8
        if (jQuery.browser.msie && jQuery.browser.version.substr(0, 1) < 9) { // ie7&ie8
            jQuery('input[placeholder], textarea[placeholder]').each(function () {
                var input = jQuery(this);

                jQuery(input).val(input.attr('placeholder'));

                jQuery(input).focus(function () {
                    if (input.val() == input.attr('placeholder')) {
                        input.val('');
                    }
                });

                jQuery(input).blur(function () {
                    if (input.val() == '' || input.val() == input.attr('placeholder')) {
                        input.val(input.attr('placeholder'));
                    }
                });
            });
        }
    }

    function handleBootstrap() {
        /*Bootstrap Carousel*/
        jQuery('.carousel').carousel({
            interval: 15000,
            pause: 'hover'
        });

        /*Tooltips*/

        // jQuery('.tooltips').tooltip();
        // jQuery('.tooltips-show').tooltip('show');      
        // jQuery('.tooltips-hide').tooltip('hide');       
        // jQuery('.tooltips-toggle').tooltip('toggle');       
        // jQuery('.tooltips-destroy').tooltip('destroy'); 
        
        jQuery('.tooltips').tooltip();
        jQuery('.tooltips-show').tooltip('show');      
        jQuery('.tooltips-hide').tooltip('hide');       
        jQuery('.tooltips-toggle').tooltip('toggle');       
        jQuery('.tooltips-destroy').tooltip('destroy');       

        /*Popovers*/
        jQuery('.popovers').popover();
        jQuery('.popovers-show').popover('show');
        jQuery('.popovers-hide').popover('hide');
        jQuery('.popovers-toggle').popover('toggle');
        jQuery('.popovers-destroy').popover('destroy');
    }

    function handleSearch() {    
        // jQuery('.search-open').hide();
        // jQuery('.search').click(function () {
        //     if(jQuery('.search-btn').hasClass('fa-search')){
        //         jQuery('.search-open').fadeIn(500);
        //         jQuery('.search-open').show();
        //         // jQuery('.search-open').addClass('nodisplay');
        //         jQuery('.search-btn').removeClass('fa-search');
        //         jQuery('.search-btn').addClass('fa-times');
        //     } else {
        //         jQuery('.search-open').fadeOut(500);
        //         jQuery('.search-open').hide();
        //         // jQuery('.search-open').removeClass('nodisplay');
        //         jQuery('.search-btn').addClass('fa-search');
        //         jQuery('.search-btn').removeClass('fa-times');
        //     }   
        // }); 
    }

    function handleToggle() {
        jQuery('.list-toggle').on('click', function() {
            jQuery(this).toggleClass('active');
        });

        /*
        jQuery('#serviceList').on('shown.bs.collapse'), function() {
            jQuery(".servicedrop").addClass('glyphicon-chevron-up').removeClass('glyphicon-chevron-down');
        }

        jQuery('#serviceList').on('hidden.bs.collapse'), function() {
            jQuery(".servicedrop").addClass('glyphicon-chevron-down').removeClass('glyphicon-chevron-up');
        }
        */
    }

    function displaySideNavNR() {
        jQuery('.graphs-viewer').show();
        jQuery('#gviewer-btn').hide();
        toggleSideNavAjax(1);
    }
    function hideSideNavNR() {
        jQuery('.graphs-viewer').hide();
        jQuery('#gviewer-btn').show();
        toggleSideNavAjax(-1);

        // $.ajax({
        //     type: "POST",
        //     dataType: "html",
        //     url: "displaySideMenu.php", //Relative or absolute path to response.php file
        //     data: {
        //         "displaySideMenu": false
        //     },
        //     success: function(data) {
        //         console.log("Form submitted successfully.\nReturned json: ");
        //         console.log("Data returned: " + data);
        //     }
        // });

    }

    function toggleSideNavAjax(is_side_nav) {
        // console.log("[toggleSideNavAjax func]  is_side_nav: " + is_side_nav);
        $.ajax({
            type: "POST",
            dataType: "html",
            url: "displaySideMenu.php", //Relative or absolute path to response.php file
            data: {
                "displaySideMenu": is_side_nav
            },
            success: function(data) {
                // console.log("Form submitted successfully.\nReturned json: ");
                // console.log("Data returned: " + data);
            }
        });
    } 

    function handleSwitcher() {    

        jQuery('.graphs-viewer-btn').click(function () {
            displaySideNavNR();
        });

        jQuery('.graph-viewer-close').click(function () {
            hideSideNavNR();
            // jQuery('.graphs-viewer').hide();
            // jQuery('#gviewer-btn').show();
        });



        jQuery('.style-switcher-btn').click(function () {
            jQuery('.style-switcher').show();
        });

        jQuery('.theme-close').click(function () {
            jQuery('.style-switcher').hide();
        });
        
    }

    function handleBoxed() {
        jQuery('.boxed-layout-btn').click(function(){
            jQuery(this).addClass("active-switcher-btn");
            jQuery(".wide-layout-btn").removeClass("active-switcher-btn");
            jQuery("body").addClass("boxed-layout container");
        });
        jQuery('.wide-layout-btn').click(function(){
            jQuery(this).addClass("active-switcher-btn");
            jQuery(".boxed-layout-btn").removeClass("active-switcher-btn");
            jQuery("body").removeClass("boxed-layout container");
        });
    }

    function handleHeader() {
         jQuery(window).scroll(function() {
            if (jQuery(window).scrollTop()>100){
                jQuery(".header-fixed .header").addClass("header-fixed-shrink");
            }
            else {
                jQuery(".header-fixed .header").removeClass("header-fixed-shrink");
            }
        });
    }

    return {
        init: function () {
            handleBootstrap();
            handleIEFixes();
            handleSearch();
            handleToggle();
            handleSwitcher();
            handleBoxed();
            handleHeader();
        },

        initSliders: function () {
            jQuery('#clients-flexslider').flexslider({
                animation: "slide",
                easing: "swing",
                animationLoop: true,
                itemWidth: 1,
                itemMargin: 1,
                minItems: 2,
                maxItems: 9,
                controlNav: false,
                directionNav: false,
                move: 2
            });
            
            jQuery('#clients-flexslider1').flexslider({
                animation: "slide",
                easing: "swing",
                animationLoop: true,
                itemWidth: 1,
                itemMargin: 1,
                minItems: 2,
                maxItems: 5,
                controlNav: false,
                directionNav: false,
                move: 2
            });
            
            jQuery('#photo-flexslider').flexslider({
                animation: "slide",
                controlNav: false,
                animationLoop: false,
                itemWidth: 80,
                itemMargin: 0
            }); 
            
            jQuery('#testimonal_carousel').collapse({
                toggle: false
            });
        },

        initFancybox: function () {
            jQuery(".fancybox-button").fancybox({
            groupAttr: 'data-rel',
            prevEffect: 'none',
            nextEffect: 'none',
            closeBtn: true,
            helpers: {
                title: {
                    type: 'inside'
                    }
                }
            });

            jQuery(".iframe").fancybox({
                maxWidth    : 800,
                maxHeight   : 600,
                fitToView   : false,
                width       : '70%',
                height      : '70%',
                autoSize    : false,
                closeClick  : false,
                openEffect  : 'none',
                closeEffect : 'none'
            });            
        },

        initBxSlider: function () {
            jQuery('.bxslider').bxSlider({
                maxSlides: 4,
                minSlides: 4,
                slideWidth: 360,
                slideMargin: 10,
            });            

            jQuery('.bxslider1').bxSlider({
                minSlides: 3,
                maxSlides: 3,
                slideWidth: 360,
                slideMargin: 10
            });            

            jQuery('.bxslider2').bxSlider({
                minSlides: 2,
                maxSlides: 2,
                slideWidth: 360,
                slideMargin: 10
            });            
        },

        initCounter: function () {
            jQuery('.counter').counterUp({
                delay: 10,
                time: 1000
            });
        },

        initParallaxBg: function () {
            jQuery('.parallaxBg').parallax("50%", 0.2);
            jQuery('.parallaxBg1').parallax("50%", 0.4);
        },

    };

}();