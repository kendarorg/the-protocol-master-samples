using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RestServer.EF;
using Task = RestServer.EF.Task;

namespace RestServer.Controllers
{
    [Route("api/status")]
    [ApiController]
    public class StatusController : ControllerBase
    {
        private readonly EFContext _context;

        public StatusController(EFContext context)
        {
            this._context = context;
        }

        // GET: api/tasks
        [HttpGet]
        public ActionResult<String> GetStatus()
        {
            return  "OK";
        }
    }
}
