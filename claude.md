You are a front-end programmer expert. Please help me generate a general global basic layout for my project, with the following requirements:

1) File the file layouts/BasicLayout.vue and include it in the global entry file App.vue.

2) Remove the default style file main.css and its reference.

3) The overall structure should be a top-middle-bottom layout, supporting responsiveness, implemented using the Layout component from the Ant Design Vue component library.

- Top navigation bar: Create a separate GlobalHeader component. The left side displays logo.png and the website title, followed by menu items. The right side displays the logged-in user's avatar and nickname (temporarily replaced by a login button). The navigation bar uses the Menu component and supports configuring menu items.

- Middle content area: Switch pages based on routes.

- Bottom copyright information: Create a separate GlobalFooter component. Its position is always fixed at the bottom, and the content is: Original project by Programmer Lawrence Hung