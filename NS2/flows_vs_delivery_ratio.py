import matplotlib.pyplot as plt 
  
# x axis values 
x = [10,20,30,40,50] 
# corresponding y axis values 
y = [0.94,0.88,0.65,0.35,0.21] 
  

# plotting the points  
plt.plot(x, y, color='violet', linewidth = 2, 
         marker='o', markerfacecolor='red', markersize=8) 
  
#fig, ax=plt.subplots()
# setting x and y axis range 
#plt.plot(range(5));
plt.ylim(0.0,1.0) 
#plt.xlim(100,1500) 

#plt.yticks(y)
plt.xticks(x)

#ax.plot(x,y)
  
# naming the x axis 
plt.xlabel('X axis: No. of Flows') 
# naming the y axis 
plt.ylabel('Y axis: Delivery Ratio') 
  
# giving a title to my graph 
plt.title('No. of Flows vs Delivery ratio') 
  
plt.grid()
# function to show the plot 
plt.show()
