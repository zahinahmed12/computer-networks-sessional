import matplotlib.pyplot as plt 
  
# x axis values 
x = [20,40,60,80,100] 
# corresponding y axis values 
y = [0.004,0.11,0.16,0.23,0.33] 
  

# plotting the points  
plt.plot(x, y, color='green', linewidth = 2, 
         marker='o', markerfacecolor='red', markersize=8) 
  
#fig, ax=plt.subplots()
# setting x and y axis range 
#plt.plot(range(5));
plt.ylim(0.0,0.5) 
#plt.xlim(100,1500) 

#plt.yticks(y)
plt.xticks(x)

#ax.plot(x,y)
  
# naming the x axis 
plt.xlabel('X axis: No. of Nodes') 
# naming the y axis 
plt.ylabel('Y axis: Drop Ratio') 
  
# giving a title to my graph 
plt.title('No. of Nodes vs Drop ratio') 
  
plt.grid()
# function to show the plot 
plt.show()
