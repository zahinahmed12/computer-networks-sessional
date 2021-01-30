import matplotlib.pyplot as plt 
  
# x axis values 
x = [250,500,750,1000,1250] 
# corresponding y axis values 
y = [0.184,1.006,0.655,0.799,0.683] 
  

# plotting the points  
plt.plot(x, y, color='blue', linewidth = 2, 
         marker='o', markerfacecolor='red', markersize=8) 
  
#fig, ax=plt.subplots()
# setting x and y axis range 
#plt.plot(range(5));
plt.ylim(0.1,1.05) 
#plt.xlim(100,1500) 

#plt.yticks(y)
plt.xticks(x)

#ax.plot(x,y)
  
# naming the x axis 
plt.xlabel('X axis: Area') 
# naming the y axis 
plt.ylabel('Y axis: Avg. Delay') 
  
# giving a title to my graph 
plt.title('Area vs Avg. delay') 
  
plt.grid()
# function to show the plot 
plt.show()
