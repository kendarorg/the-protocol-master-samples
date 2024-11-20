using Microsoft.EntityFrameworkCore;
using RestServer.EF;


var configuration = new ConfigurationBuilder()
        .AddEnvironmentVariables()
        .AddCommandLine(args)
        .AddJsonFile("appsettings.json")
        .Build();
var mainConnectionString = configuration.GetConnectionString("Main");

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddDbContext<EFContext>(opt =>
{
    //opt.UseInMemoryDatabase("TodoList");
    opt.UseMySQL(mainConnectionString);
});

var app = builder.Build();
app.UseCors(builder =>
{
    builder
       .WithOrigins("*")
       .SetIsOriginAllowedToAllowWildcardSubdomains()
       .AllowAnyHeader()
       .WithMethods("GET", "PUT", "POST", "DELETE", "OPTIONS")
       .SetPreflightMaxAge(TimeSpan.FromSeconds(3600));
}
);

app.UseSwagger();
app.UseSwaggerUI();

app.UseAuthorization();

app.MapControllers();

app.Run();
